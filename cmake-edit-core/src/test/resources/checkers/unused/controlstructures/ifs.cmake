set(var val) # used then we go through 'else' branch.
if ($ENV{a})
   set(var val2)
endif()
message(${var})

if ($ENV{a})
    if (${ENV{b})
        set(var3 val3)
    endif()
endif()
message(${var3})