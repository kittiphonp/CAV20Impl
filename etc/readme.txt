This folder contains some resources that we used to compile the codes.
They are already applied to the codes in this repository; here we leave them for reference.

** fix-lpsolve-compilation.patch
This is a patch to resolve a compile issue on lp solver, provided Joachim Klein. 
See the following discussion for the details:
https://groups.google.com/g/prismmodelchecker/c/gtVatHAir90?pli=1

**fix-ambiguous-reference
If one uses java 9 or newer, the original source of PRISM-games 2.0 suffers an error due to an ambiguous call to Module. 
In the codes of this repository the issue is resolved by explicitly declaring parser.ast.Module in each relevant .java source.
This folder contains these modified .java source as well as a script that copy&pastes them to appropriate directories.