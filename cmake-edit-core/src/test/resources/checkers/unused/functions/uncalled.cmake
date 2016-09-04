function(localscope)
    set(local unused) # problem
    set(local2 used) # no problem
    message(${local2})
endfunction()

function(parentscope)
    set(global foo PARENT_SCOPE)
endfunction()
