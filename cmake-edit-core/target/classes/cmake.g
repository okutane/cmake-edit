grammar cmake;

file         :  file_element* EOF ;

file_element :  command_invocation line_ending |
                  (bracket_comment|space)* line_ending ;
line_ending  :  line_comment? newline ;
space        :  ' ' ;
newline      :  '\n' ;

command_invocation  :  space* identifier space* '(' arguments ')' ;
identifier          :  .+ ;
arguments           :  argument? separated_arguments* ;
separated_arguments :  separation+ argument? |
                         separation* '(' arguments ')' ;
separation          :  space | line_ending ;

argument :  bracket_argument | quoted_argument | unquoted_argument ;

quoted_argument     :  '"' quoted_element* '"' ;
quoted_element      :  .* | quoted_continuation ; // todo
quoted_continuation :  .* ; // todo

unquoted_argument :  unquoted_element+ | unquoted_legacy ;
unquoted_element  :  .* ; // todo
unquoted_legacy   :  .* ; // todo

line_comment
	: # .* ;

bracket_comment
	: # bracket_argument ;

bracket_argument :  bracket_open bracket_content bracket_close ;
bracket_open     :  '['+ ; // todo
bracket_content  :  .* ;
bracket_close    :  ']'+ ; // todo