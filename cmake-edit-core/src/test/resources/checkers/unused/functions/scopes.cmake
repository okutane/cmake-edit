function(useglobals)
    message(${var})
    set(var2 val2)
endfunction()

set(var val) # used implicitly in useglobals
useglobals()
message(${var2}) # should be set in useglobals