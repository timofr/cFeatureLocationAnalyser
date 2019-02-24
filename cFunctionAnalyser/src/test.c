#include <stdio.h>

#ifdef BLABLA
int blabla() {
	printf("Bla");
#else BLABLA
int bla() {
#endif
	printf("Bla\n");
}

#ifdef TEST
int test() {
	printf("Foo\n");
}

#else
int test() {
	printf("Bar\n");
}

#endif
int main(int argc, char* argv) {
	test();
	#ifdef BLABLA
	blabla();
	#else
	bla();
}