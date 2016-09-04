set(var val) # path

macro(mymacro text)
	message(${text})
endmacro()

mymacro("${var}")