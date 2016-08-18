function(init_a)
    set(a 1) # unused (wrong scope)
endfunction()

function(init_b)
    set(b 1 PARENT_SCOPE)
endfunction()

init_a()
init_b()
message(${a} ${b})