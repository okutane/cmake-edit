set(varFound 0) # ok
get_cmake_property(_variableNames VARIABLES)
foreach (_variableName ${_variableNames})
    if (${_variableName} EQUAL varFound)
        set(varFound 1) # ok
        break()
    endif()
    message(STATUS "${_variableName}=${${_variableName}}")
endforeach()

message(${varFound})