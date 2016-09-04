function(init varname)
    set(${varname} val PARENT_SCOPE) # no problem
endfunction()

init(a)
message(${a})