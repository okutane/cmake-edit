function(use arg)
    message(${arg})
endfunction()

function(ignore arg)
endfunction()

set(a a) # ok
use(a)

set(b b) # problem
ignore(b)