# P4B

### Introduction

P4B is an automatic verification tool for P4 programs. P4B receives AST of  a P4 program in json format generated by [p4c](https://github.com/p4lang/p4c). Our verification is based on [Boogie IVL](https://www.microsoft.com/en-us/research/project/boogie-an-intermediate-verification-language/). The generated Boogie programs can be verified by [Boogie](https://github.com/boogie-org/boogie) or [Corral](https://github.com/boogie-org/corral).

We support four properties:

- header validity
- header stack out-of-bound
- implicit drop
- modification of read-only fields

### Dependencies

- Z3: libz3.so and libz3java.so (Linux)
- Boogie Verifier

### Usage

P4B by default translates the input P4 program into Boogie program. If provided options, P4B can verify the properties above. Note that header validity, header stack out-of-bound and modification of read-only fields can be verified during translation, while implicit drop needs to be verified by Boogie verifier.

Options:

```
java -jar p4b.jar [options]* <inputfiles> <outputfiles>

[options]:
  -h                     show usage
  -headerValidity        check header validity
  -headerStackBound      check header stack out-of-bounds error
  -implicitDrop          check implicit drops, which occur when egress_spec is not assigned
  -readOnly              check modification of read-only fields
  -all                   verify all the properties above
  -control               add control plane constraints (developing)
  -rAssertion            remove redundant assertion statements
  -log                   show debug information
```

Example:

```
java -jar p4b.jar ./benchmarks/axon/p416-axon-ppc.json ./benchmarks/axon/p416-axon-ppc.bpl
```

### Experiment

We collected open source programs on github as benchmarks.