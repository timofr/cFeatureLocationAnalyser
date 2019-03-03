
#ifdef	CONFIG_USB_OTG

/*
 * The OMAP OTG controller handles most of the OTG state transitions.
 *
 * We translate isp1301 outputs (mostly voltage comparator status) into
 * OTG inputs; OTG outputs (mostly pullup/pulldown controls) and HNP state
 * flags into isp1301 inputs ... and infer state transitions.
 */

#ifdef	VERBOSE

static void check_state(struct isp1301 *isp, const char *tag)
{
	enum usb_otg_state	state = OTG_STATE_UNDEFINED;
	u8			fsm = omap_readw(OTG_TEST) & 0x0ff;
	unsigned		extra = 0;

	switch (fsm) {

	/* default-b */
	case 0x0:
		state = OTG_STATE_B_IDLE;
		break;
	case 0x3:
	case 0x7:
		extra = 1;
	case 0x1:
		state = OTG_STATE_B_PERIPHERAL;
		break;
	case 0x11:
		state = OTG_STATE_B_SRP_INIT;
		break;

	/* extra dual-role default-b states */
	case 0x12:
	case 0x13:
	case 0x16:
		extra = 1;
	case 0x17:
		state = OTG_STATE_B_WAIT_ACON;
		break;
	case 0x34:
		state = OTG_STATE_B_HOST;
		break;

	/* default-a */
	case 0x36:
		state = OTG_STATE_A_IDLE;
		break;
	case 0x3c:
		state = OTG_STATE_A_WAIT_VFALL;
		break;
	case 0x7d:
		state = OTG_STATE_A_VBUS_ERR;
		break;
	case 0x9e:
	case 0x9f:
		extra = 1;
	case 0x89:
		state = OTG_STATE_A_PERIPHERAL;
		break;
	case 0xb7:
		state = OTG_STATE_A_WAIT_VRISE;
		break;
	case 0xb8:
		state = OTG_STATE_A_WAIT_BCON;
		break;
	case 0xb9:
		state = OTG_STATE_A_HOST;
		break;
	case 0xba:
		state = OTG_STATE_A_SUSPEND;
		break;
	default:
		break;
	}
	if (isp->phy.otg->state == state && !extra)
		return;
	pr_debug("otg: %s FSM %s/%02x, %s, %06x\n", tag,
		usb_otg_state_string(state), fsm, state_name(isp),
		omap_readl(OTG_CTRL));
}

#else

static inline void check_state(struct isp1301 *isp, const char *tag) { }

#endif

/* outputs from ISP1301_INTERRUPT_SOURCE */
static void update_otg1(struct isp1301 *isp, u8 int_src)
{
	u32	otg_ctrl;

	otg_ctrl = omap_readl(OTG_CTRL) & OTG_CTRL_MASK;
	otg_ctrl &= ~OTG_XCEIV_INPUTS;
	otg_ctrl &= ~(OTG_ID|OTG_ASESSVLD|OTG_VBUSVLD);

	if (int_src & INTR_SESS_VLD)
		otg_ctrl |= OTG_ASESSVLD;
	else if (isp->phy.otg->state == OTG_STATE_A_WAIT_VFALL) {
		a_idle(isp, "vfall");
		otg_ctrl &= ~OTG_CTRL_BITS;
	}
	if (int_src & INTR_VBUS_VLD)
		otg_ctrl |= OTG_VBUSVLD;
	if (int_src & INTR_ID_GND) {		/* default-A */
		if (isp->phy.otg->state == OTG_STATE_B_IDLE
				|| isp->phy.otg->state
					== OTG_STATE_UNDEFINED) {
			a_idle(isp, "init");
			return;
		}
	} else {				/* default-B */
		otg_ctrl |= OTG_ID;
		if (isp->phy.otg->state == OTG_STATE_A_IDLE
			|| isp->phy.otg->state == OTG_STATE_UNDEFINED) {
			b_idle(isp, "init");
			return;
		}
	}
	omap_writel(otg_ctrl, OTG_CTRL);
}

/* outputs from ISP1301_OTG_STATUS */
static void update_otg2(struct isp1301 *isp, u8 otg_status)
{
	u32	otg_ctrl;

	otg_ctrl = omap_readl(OTG_CTRL) & OTG_CTRL_MASK;
	otg_ctrl &= ~OTG_XCEIV_INPUTS;
	otg_ctrl &= ~(OTG_BSESSVLD | OTG_BSESSEND);
	if (otg_status & OTG_B_SESS_VLD)
		otg_ctrl |= OTG_BSESSVLD;
	else if (otg_status & OTG_B_SESS_END)
		otg_ctrl |= OTG_BSESSEND;
	omap_writel(otg_ctrl, OTG_CTRL);
}

/* inputs going to ISP1301 */
static void otg_update_isp(struct isp1301 *isp)
{
	u32	otg_ctrl, otg_change;
	u8	set = OTG1_DM_PULLDOWN, clr = OTG1_DM_PULLUP;

	otg_ctrl = omap_readl(OTG_CTRL);
	otg_change = otg_ctrl ^ isp->last_otg_ctrl;
	isp->last_otg_ctrl = otg_ctrl;
	otg_ctrl = otg_ctrl & OTG_XCEIV_INPUTS;

	switch (isp->phy.otg->state) {
	case OTG_STATE_B_IDLE:
	case OTG_STATE_B_PERIPHERAL:
	case OTG_STATE_B_SRP_INIT:
		if (!(otg_ctrl & OTG_PULLUP)) {
			// if (otg_ctrl & OTG_B_HNPEN) {
			if (isp->phy.otg->gadget->b_hnp_enable) {
				isp->phy.otg->state = OTG_STATE_B_WAIT_ACON;
				pr_debug("  --> b_wait_acon\n");
			}
			goto pulldown;
		}
pullup:
		set |= OTG1_DP_PULLUP;
		clr |= OTG1_DP_PULLDOWN;
		break;
	case OTG_STATE_A_SUSPEND:
	case OTG_STATE_A_PERIPHERAL:
		if (otg_ctrl & OTG_PULLUP)
			goto pullup;
		/* FALLTHROUGH */
	// case OTG_STATE_B_WAIT_ACON:
	default:
pulldown:
		set |= OTG1_DP_PULLDOWN;
		clr |= OTG1_DP_PULLUP;
		break;
	}

#	define toggle(OTG,ISP) do { \
		if (otg_ctrl & OTG) set |= ISP; \
		else clr |= ISP; \
		} while (0)

	if (!(isp->phy.otg->host))
		otg_ctrl &= ~OTG_DRV_VBUS;

	switch (isp->phy.otg->state) {
	case OTG_STATE_A_SUSPEND:
		if (otg_ctrl & OTG_DRV_VBUS) {
			set |= OTG1_VBUS_DRV;
			break;
		}
		/* HNP failed for some reason (A_AIDL_BDIS timeout) */
		notresponding(isp);

		/* FALLTHROUGH */
	case OTG_STATE_A_VBUS_ERR:
		isp->phy.otg->state = OTG_STATE_A_WAIT_VFALL;
		pr_debug("  --> a_wait_vfall\n");
		/* FALLTHROUGH */
	case OTG_STATE_A_WAIT_VFALL:
		/* FIXME usbcore thinks port power is still on ... */
		clr |= OTG1_VBUS_DRV;
		break;
	case OTG_STATE_A_IDLE:
		if (otg_ctrl & OTG_DRV_VBUS) {
			isp->phy.otg->state = OTG_STATE_A_WAIT_VRISE;
			pr_debug("  --> a_wait_vrise\n");
		}
		/* FALLTHROUGH */
	default:
		toggle(OTG_DRV_VBUS, OTG1_VBUS_DRV);
	}

	toggle(OTG_PU_VBUS, OTG1_VBUS_CHRG);
	toggle(OTG_PD_VBUS, OTG1_VBUS_DISCHRG);

#	undef toggle

	isp1301_set_bits(isp, ISP1301_OTG_CONTROL_1, set);
	isp1301_clear_bits(isp, ISP1301_OTG_CONTROL_1, clr);

	/* HNP switch to host or peripheral; and SRP */
	if (otg_change & OTG_PULLUP) {
		u32 l;

		switch (isp->phy.otg->state) {
		case OTG_STATE_B_IDLE:
			if (clr & OTG1_DP_PULLUP)
				break;
			isp->phy.otg->state = OTG_STATE_B_PERIPHERAL;
			pr_debug("  --> b_peripheral\n");
			break;
		case OTG_STATE_A_SUSPEND:
			if (clr & OTG1_DP_PULLUP)
				break;
			isp->phy.otg->state = OTG_STATE_A_PERIPHERAL;
			pr_debug("  --> a_peripheral\n");
			break;
		default:
			break;
		}
		l = omap_readl(OTG_CTRL);
		l |= OTG_PULLUP;
		omap_writel(l, OTG_CTRL);
	}

	check_state(isp, __func__);
	dump_regs(isp, "otg->isp1301");
}

static irqreturn_t omap_otg_irq(int irq, void *_isp)
{
	u16		otg_irq = omap_readw(OTG_IRQ_SRC);
	u32		otg_ctrl;
	int		ret = IRQ_NONE;
	struct isp1301	*isp = _isp;
	struct usb_otg	*otg = isp->phy.otg;

	/* update ISP1301 transceiver from OTG controller */
	if (otg_irq & OPRT_CHG) {
		omap_writew(OPRT_CHG, OTG_IRQ_SRC);
		isp1301_defer_work(isp, WORK_UPDATE_ISP);
		ret = IRQ_HANDLED;

	/* SRP to become b_peripheral failed */
	} else if (otg_irq & B_SRP_TMROUT) {
		pr_debug("otg: B_SRP_TIMEOUT, %06x\n", omap_readl(OTG_CTRL));
		notresponding(isp);

		/* gadget drivers that care should monitor all kinds of
		 * remote wakeup (SRP, normal) using their own timer
		 * to give "check cable and A-device" messages.
		 */
		if (isp->phy.otg->state == OTG_STATE_B_SRP_INIT)
			b_idle(isp, "srp_timeout");

		omap_writew(B_SRP_TMROUT, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

	/* HNP to become b_host failed */
	} else if (otg_irq & B_HNP_FAIL) {
		pr_debug("otg: %s B_HNP_FAIL, %06x\n",
				state_name(isp), omap_readl(OTG_CTRL));
		notresponding(isp);

		otg_ctrl = omap_readl(OTG_CTRL);
		otg_ctrl |= OTG_BUSDROP;
		otg_ctrl &= OTG_CTRL_MASK & ~OTG_XCEIV_INPUTS;
		omap_writel(otg_ctrl, OTG_CTRL);

		/* subset of b_peripheral()... */
		isp->phy.otg->state = OTG_STATE_B_PERIPHERAL;
		pr_debug("  --> b_peripheral\n");

		omap_writew(B_HNP_FAIL, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

	/* detect SRP from B-device ... */
	} else if (otg_irq & A_SRP_DETECT) {
		pr_debug("otg: %s SRP_DETECT, %06x\n",
				state_name(isp), omap_readl(OTG_CTRL));

		isp1301_defer_work(isp, WORK_UPDATE_OTG);
		switch (isp->phy.otg->state) {
		case OTG_STATE_A_IDLE:
			if (!otg->host)
				break;
			isp1301_defer_work(isp, WORK_HOST_RESUME);
			otg_ctrl = omap_readl(OTG_CTRL);
			otg_ctrl |= OTG_A_BUSREQ;
			otg_ctrl &= ~(OTG_BUSDROP|OTG_B_BUSREQ)
					& ~OTG_XCEIV_INPUTS
					& OTG_CTRL_MASK;
			omap_writel(otg_ctrl, OTG_CTRL);
			break;
		default:
			break;
		}

		omap_writew(A_SRP_DETECT, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

	/* timer expired:  T(a_wait_bcon) and maybe T(a_wait_vrise)
	 * we don't track them separately
	 */
	} else if (otg_irq & A_REQ_TMROUT) {
		otg_ctrl = omap_readl(OTG_CTRL);
		pr_info("otg: BCON_TMOUT from %s, %06x\n",
				state_name(isp), otg_ctrl);
		notresponding(isp);

		otg_ctrl |= OTG_BUSDROP;
		otg_ctrl &= ~OTG_A_BUSREQ & OTG_CTRL_MASK & ~OTG_XCEIV_INPUTS;
		omap_writel(otg_ctrl, OTG_CTRL);
		isp->phy.otg->state = OTG_STATE_A_WAIT_VFALL;

		omap_writew(A_REQ_TMROUT, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

	/* A-supplied voltage fell too low; overcurrent */
	} else if (otg_irq & A_VBUS_ERR) {
		otg_ctrl = omap_readl(OTG_CTRL);
		printk(KERN_ERR "otg: %s, VBUS_ERR %04x ctrl %06x\n",
			state_name(isp), otg_irq, otg_ctrl);

		otg_ctrl |= OTG_BUSDROP;
		otg_ctrl &= ~OTG_A_BUSREQ & OTG_CTRL_MASK & ~OTG_XCEIV_INPUTS;
		omap_writel(otg_ctrl, OTG_CTRL);
		isp->phy.otg->state = OTG_STATE_A_VBUS_ERR;

		omap_writew(A_VBUS_ERR, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

	/* switch driver; the transceiver code activates it,
	 * ungating the udc clock or resuming OHCI.
	 */
	} else if (otg_irq & DRIVER_SWITCH) {
		int	kick = 0;

		otg_ctrl = omap_readl(OTG_CTRL);
		printk(KERN_NOTICE "otg: %s, SWITCH to %s, ctrl %06x\n",
				state_name(isp),
				(otg_ctrl & OTG_DRIVER_SEL)
					? "gadget" : "host",
				otg_ctrl);
		isp1301_defer_work(isp, WORK_UPDATE_ISP);

		/* role is peripheral */
		if (otg_ctrl & OTG_DRIVER_SEL) {
			switch (isp->phy.otg->state) {
			case OTG_STATE_A_IDLE:
				b_idle(isp, __func__);
				break;
			default:
				break;
			}
			isp1301_defer_work(isp, WORK_UPDATE_ISP);

		/* role is host */
		} else {
			if (!(otg_ctrl & OTG_ID)) {
				otg_ctrl &= OTG_CTRL_MASK & ~OTG_XCEIV_INPUTS;
				omap_writel(otg_ctrl | OTG_A_BUSREQ, OTG_CTRL);
			}

			if (otg->host) {
				switch (isp->phy.otg->state) {
				case OTG_STATE_B_WAIT_ACON:
					isp->phy.otg->state = OTG_STATE_B_HOST;
					pr_debug("  --> b_host\n");
					kick = 1;
					break;
				case OTG_STATE_A_WAIT_BCON:
					isp->phy.otg->state = OTG_STATE_A_HOST;
					pr_debug("  --> a_host\n");
					break;
				case OTG_STATE_A_PERIPHERAL:
					isp->phy.otg->state = OTG_STATE_A_WAIT_BCON;
					pr_debug("  --> a_wait_bcon\n");
					break;
				default:
					break;
				}
				isp1301_defer_work(isp, WORK_HOST_RESUME);
			}
		}

		omap_writew(DRIVER_SWITCH, OTG_IRQ_SRC);
		ret = IRQ_HANDLED;

		if (kick)
			usb_bus_start_enum(otg->host, otg->host->otg_port);
	}

	check_state(isp, __func__);
	return ret;
}

static struct platform_device *otg_dev;

static int isp1301_otg_init(struct isp1301 *isp)
{
	u32 l;

	if (!otg_dev)
		return -ENODEV;

	dump_regs(isp, __func__);
	/* some of these values are board-specific... */
	l = omap_readl(OTG_SYSCON_2);
	l |= OTG_EN
		/* for B-device: */
		| SRP_GPDATA		/* 9msec Bdev D+ pulse */
		| SRP_GPDVBUS		/* discharge after VBUS pulse */
		// | (3 << 24)		/* 2msec VBUS pulse */
		/* for A-device: */
		| (0 << 20)		/* 200ms nominal A_WAIT_VRISE timer */
		| SRP_DPW		/* detect 167+ns SRP pulses */
		| SRP_DATA | SRP_VBUS	/* accept both kinds of SRP pulse */
		;
	omap_writel(l, OTG_SYSCON_2);

	update_otg1(isp, isp1301_get_u8(isp, ISP1301_INTERRUPT_SOURCE));
	update_otg2(isp, isp1301_get_u8(isp, ISP1301_OTG_STATUS));

	check_state(isp, __func__);
	pr_debug("otg: %s, %s %06x\n",
			state_name(isp), __func__, omap_readl(OTG_CTRL));

	omap_writew(DRIVER_SWITCH | OPRT_CHG
			| B_SRP_TMROUT | B_HNP_FAIL
			| A_VBUS_ERR | A_SRP_DETECT | A_REQ_TMROUT, OTG_IRQ_EN);

	l = omap_readl(OTG_SYSCON_2);
	l |= OTG_EN;
	omap_writel(l, OTG_SYSCON_2);

	return 0;
}

static int otg_probe(struct platform_device *dev)
{
	// struct omap_usb_config *config = dev->platform_data;

	otg_dev = dev;
	return 0;
}

static int otg_remove(struct platform_device *dev)
{
	otg_dev = NULL;
	return 0;
}

static struct platform_driver omap_otg_driver = {
	.probe		= otg_probe,
	.remove		= otg_remove,
	.driver		= {
		.name	= "omap_otg",
	},
};

static int otg_bind(struct isp1301 *isp)
{
	int	status;

	if (otg_dev)
		return -EBUSY;

	status = platform_driver_register(&omap_otg_driver);
	if (status < 0)
		return status;

	if (otg_dev)
		status = request_irq(otg_dev->resource[1].start, omap_otg_irq,
				0, DRIVER_NAME, isp);
	else
		status = -ENODEV;

	if (status < 0)
		platform_driver_unregister(&omap_otg_driver);
	return status;
}

static void otg_unbind(struct isp1301 *isp)
{
	if (!otg_dev)
		return;
	free_irq(otg_dev->resource[1].start, isp);
}

#else

/* OTG controller isn't clocked */

#endif	/* CONFIG_USB_OTG */

/*-------------------------------------------------------------------------*/

static void b_peripheral(struct isp1301 *isp)
{
	u32 l;

	l = omap_readl(OTG_CTRL) & OTG_XCEIV_OUTPUTS;
	omap_writel(l, OTG_CTRL);

	usb_gadget_vbus_connect(isp->phy.otg->gadget);

#ifdef	CONFIG_USB_OTG
	enable_vbus_draw(isp, 8);
	otg_update_isp(isp);
#else
	enable_vbus_draw(isp, 100);
	/* UDC driver just set OTG_BSESSVLD */
	isp1301_set_bits(isp, ISP1301_OTG_CONTROL_1, OTG1_DP_PULLUP);
	isp1301_clear_bits(isp, ISP1301_OTG_CONTROL_1, OTG1_DP_PULLDOWN);
	isp->phy.otg->state = OTG_STATE_B_PERIPHERAL;
	pr_debug("  --> b_peripheral\n");
	dump_regs(isp, "2periph");
#endif
}

static void isp_update_otg(struct isp1301 *isp, u8 stat)
{
	struct usb_otg		*otg = isp->phy.otg;
	u8			isp_stat, isp_bstat;
	enum usb_otg_state	state = isp->phy.otg->state;

	if (stat & INTR_BDIS_ACON)
		pr_debug("OTG:  BDIS_ACON, %s\n", state_name(isp));

	/* start certain state transitions right away */
	isp_stat = isp1301_get_u8(isp, ISP1301_INTERRUPT_SOURCE);
	if (isp_stat & INTR_ID_GND) {
		if (otg->default_a) {
			switch (state) {
			case OTG_STATE_B_IDLE:
				a_idle(isp, "idle");
				/* FALLTHROUGH */
			case OTG_STATE_A_IDLE:
				enable_vbus_source(isp);
				/* FALLTHROUGH */
			case OTG_STATE_A_WAIT_VRISE:
				/* we skip over OTG_STATE_A_WAIT_BCON, since
				 * the HC will transition to A_HOST (or
				 * A_SUSPEND!) without our noticing except
				 * when HNP is used.
				 */
				if (isp_stat & INTR_VBUS_VLD)
					isp->phy.otg->state = OTG_STATE_A_HOST;
				break;
			case OTG_STATE_A_WAIT_VFALL:
				if (!(isp_stat & INTR_SESS_VLD))
					a_idle(isp, "vfell");
				break;
			default:
				if (!(isp_stat & INTR_VBUS_VLD))
					isp->phy.otg->state = OTG_STATE_A_VBUS_ERR;
				break;
			}
			isp_bstat = isp1301_get_u8(isp, ISP1301_OTG_STATUS);
		} else {
			switch (state) {
			case OTG_STATE_B_PERIPHERAL:
			case OTG_STATE_B_HOST:
			case OTG_STATE_B_WAIT_ACON:
				usb_gadget_vbus_disconnect(otg->gadget);
				break;
			default:
				break;
			}
			if (state != OTG_STATE_A_IDLE)
				a_idle(isp, "id");
			if (otg->host && state == OTG_STATE_A_IDLE)
				isp1301_defer_work(isp, WORK_HOST_RESUME);
			isp_bstat = 0;
		}
	} else {
		u32 l;

		/* if user unplugged mini-A end of cable,
		 * don't bypass A_WAIT_VFALL.
		 */
		if (otg->default_a) {
			switch (state) {
			default:
				isp->phy.otg->state = OTG_STATE_A_WAIT_VFALL;
				break;
			case OTG_STATE_A_WAIT_VFALL:
				state = OTG_STATE_A_IDLE;
				/* hub_wq may take a while to notice and
				 * handle this disconnect, so don't go
				 * to B_IDLE quite yet.
				 */
				break;
			case OTG_STATE_A_IDLE:
				host_suspend(isp);
				isp1301_clear_bits(isp, ISP1301_MODE_CONTROL_1,
						MC1_BDIS_ACON_EN);
				isp->phy.otg->state = OTG_STATE_B_IDLE;
				l = omap_readl(OTG_CTRL) & OTG_CTRL_MASK;
				l &= ~OTG_CTRL_BITS;
				omap_writel(l, OTG_CTRL);
				break;
			case OTG_STATE_B_IDLE:
				break;
			}
		}
		isp_bstat = isp1301_get_u8(isp, ISP1301_OTG_STATUS);

		switch (isp->phy.otg->state) {
		case OTG_STATE_B_PERIPHERAL:
		case OTG_STATE_B_WAIT_ACON:
		case OTG_STATE_B_HOST:
			if (likely(isp_bstat & OTG_B_SESS_VLD))
				break;
			enable_vbus_draw(isp, 0);
#ifndef	CONFIG_USB_OTG
			/* UDC driver will clear OTG_BSESSVLD */
			isp1301_set_bits(isp, ISP1301_OTG_CONTROL_1,
						OTG1_DP_PULLDOWN);
			isp1301_clear_bits(isp, ISP1301_OTG_CONTROL_1,
						OTG1_DP_PULLUP);
			dump_regs(isp, __func__);
#endif
			/* FALLTHROUGH */
		case OTG_STATE_B_SRP_INIT:
			b_idle(isp, __func__);
			l = omap_readl(OTG_CTRL) & OTG_XCEIV_OUTPUTS;
			omap_writel(l, OTG_CTRL);
			/* FALLTHROUGH */
		case OTG_STATE_B_IDLE:
			if (otg->gadget && (isp_bstat & OTG_B_SESS_VLD)) {
#ifdef	CONFIG_USB_OTG
				update_otg1(isp, isp_stat);
				update_otg2(isp, isp_bstat);
#endif
				b_peripheral(isp);
			} else if (!(isp_stat & (INTR_VBUS_VLD|INTR_SESS_VLD)))
				isp_bstat |= OTG_B_SESS_END;
			break;
		case OTG_STATE_A_WAIT_VFALL:
			break;
		default:
			pr_debug("otg: unsupported b-device %s\n",
				state_name(isp));
			break;
		}
	}

	if (state != isp->phy.otg->state)
		pr_debug("  isp, %s -> %s\n",
				usb_otg_state_string(state), state_name(isp));

#ifdef	CONFIG_USB_OTG
	/* update the OTG controller state to match the isp1301; may
	 * trigger OPRT_CHG irqs for changes going to the isp1301.
	 */
	update_otg1(isp, isp_stat);
	update_otg2(isp, isp_bstat);
	check_state(isp, __func__);
#endif

	dump_regs(isp, "isp1301->otg");
}

