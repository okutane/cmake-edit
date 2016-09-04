function(foo)
    set(local val1) # problem
    return()
    message(${local}) # unreachable
endfunction()

set(global val2) # problem
return()
message(${global}) # unreachable