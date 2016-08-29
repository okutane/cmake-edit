function(useglobals)
    message(${var})
endfunction()

set(var val) # used implicitly in useglobals
useglobals()