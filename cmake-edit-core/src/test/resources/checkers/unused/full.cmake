set(VAR_1 value1) # value1 is not used
set(VAR_2 value2)
set(USED_VAR used_value)
set(UNUSED_VAR unused_value) # unused_value is not used

if (SOMETHING)
   set(VAR_1 value11)
   set(VAR_2 value22)
   set(VAR_3 ${USED_VAR})
else()
   set(VAR_1 value11)
   set(VAR_3 value3)
endif()

include_directories(${VAR_1} ${VAR_2})
set(CMAKE_XXX ${VAR_3})