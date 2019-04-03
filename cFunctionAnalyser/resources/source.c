struct vm_area_struct *vma_merge(struct mm_struct *mm,
			struct vm_area_struct *prev, unsigned long addr,
			unsigned long end, unsigned long vm_flags,
			struct anon_vma *anon_vma, struct file *file,
			pgoff_t pgoff, struct mempolicy *policy,
			struct vm_userfaultfd_ctx vm_userfaultfd_ctx)
{
	pgoff_t pglen = (end - addr) >> PAGE_SHIFT;
	struct vm_area_struct *area, *next;
	int err;

	/*
	 * We later require that vma->vm_flags == vm_flags,
	 * so this tests vma->vm_flags & VM_SPECIAL, too.
	 */
	if (vm_flags & VM_SPECIAL)
		return NULL;

	if (prev)
		next = prev->vm_next;
	else
		next = mm->mmap;
	area = next;
	if (area && area->vm_end == end)		/* cases 6, 7, 8 */
		next = next->vm_next;

	/* verify some invariant that must be enforced by the caller */
	VM_WARN_ON(prev && addr <= prev->vm_start);
	VM_WARN_ON(area && end > area->vm_end);
	VM_WARN_ON(addr >= end);

	/*
	 * Can it merge with the predecessor?
	 */
	if (prev && prev->vm_end == addr &&
			mpol_equal(vma_policy(prev), policy) &&
			can_vma_merge_after(prev, vm_flags,
					    anon_vma, file, pgoff,
					    vm_userfaultfd_ctx)) {
		/*
		 * OK, it can.  Can we now merge in the successor as well?
		 */
		if (next && end == next->vm_start &&
				mpol_equal(policy, vma_policy(next)) &&
				can_vma_merge_before(next, vm_flags,
						     anon_vma, file,
						     pgoff+pglen,
						     vm_userfaultfd_ctx) &&
				is_mergeable_anon_vma(prev->anon_vma,
						      next->anon_vma, NULL)) {
							/* cases 1, 6 */
			err = __vma_adjust(prev, prev->vm_start,
					 next->vm_end, prev->vm_pgoff, NULL,
					 prev);
		} else					/* cases 2, 5, 7 */
			err = __vma_adjust(prev, prev->vm_start,
					 end, prev->vm_pgoff, NULL, prev);
		if (err)
			return NULL;
		khugepaged_enter_vma_merge(prev, vm_flags);
		return prev;
	}

	/*
	 * Can this new request be merged in front of next?
	 */
	if (next && end == next->vm_start &&
			mpol_equal(policy, vma_policy(next)) &&
			can_vma_merge_before(next, vm_flags,
					     anon_vma, file, pgoff+pglen,
					     vm_userfaultfd_ctx)) {
		if (prev && addr < prev->vm_end)	/* case 4 */
			err = __vma_adjust(prev, prev->vm_start,
					 addr, prev->vm_pgoff, NULL, next);
		else {					/* cases 3, 8 */
			err = __vma_adjust(area, addr, next->vm_end,
					 next->vm_pgoff - pglen, NULL, next);
			/*
			 * In case 3 area is already equal to next and
			 * this is a noop, but in case 8 "area" has
			 * been removed and next was expanded over it.
			 */
			area = next;
		}
		if (err)
			return NULL;
		khugepaged_enter_vma_merge(area, vm_flags);
		return area;
	}

	return NULL;
}
