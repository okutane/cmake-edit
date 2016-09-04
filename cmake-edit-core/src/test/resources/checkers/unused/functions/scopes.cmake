function(useglobals)
    message(${var})
    set(var2 val2) # problem, forgot to add PARENT_SCOPE
    set(var3 var3 PARENT_SCOPE) # ok, used externally
endfunction()

set(var val) # used implicitly in useglobals
useglobals()
message(${var2}) # using undefined
message(${var3})