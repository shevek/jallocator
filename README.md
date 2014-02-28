# Introduction

This library allows multiple independent processes or threads to
allocate resources from a shared pool with a low probability of
collision or blocking. It is based on the principle of optimistic
concurrency: Resources returned by the allocator are _statistically
very likely_ to be free. One process (or one thread) does not block
any other, and in fact, if multiple threads are allocating from the
same allocator object in a single VM, it will be considerably faster
than using a separate allocator for each thread.

The algorithm and several variants was described by Don Knuth in
the paper
[Computer Science and its Relation to Mathematics](http://www.maa.org/sites/default/files/pdf/upload_library/22/Ford/DonaldKnuth.pdf),
and on the assumption that the BlackRock cipher used in jallocator
represents a uniform distribution, I believe it is reasonably
immediate that this library is optimal.

# Usage

The [JavaDoc API](http://shevek.github.io/jallocator/docs/javadoc/)
is available.

