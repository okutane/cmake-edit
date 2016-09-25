set(MyVar first)
while(SOMETHING)
   print(${MyVar})
   set(MyVar second) # used in 2nd iteration
endwhile(SOMETHING_ELSE)

set(MyVar first)
while(SOMETHING)
   print(${MyVar})
   set(MyVar second) # not used
   break()
endwhile(SOMETHING_ELSE)

set(MyVar first)
while(SOMETHING)
   print(${MyVar})
   set(MyVar second) # not used
   return()
endwhile(SOMETHING_ELSE)

set(MyVar first)
while(SOMETHING)
   print(${MyVar})
   set(MyVar second) # not used
   set(MyVar third) # used in 2nd iteration
endwhile(SOMETHING_ELSE)