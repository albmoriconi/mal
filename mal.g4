grammar mal;

uProgram : instruction*;

instruction : label? statementSequence;

label : NAME ('=' ADDRESS)? ':';

statementSequence : EMPTY_STATEMENT
                  | HALT_STATEMENT
                  | assignmentStatement (';' memoryStatement)? (';' controlStatement)?
                  | memoryStatement (';' controlStatement)?
                  | controlStatement
                  ;

assignmentStatement : destination '=' expression;

destination : cRegister
            | condition
            ;

expression : assignmentStatement
           | operation '<<' '8'
           | operation '>>' '1'
           | operation
           ;

operation : aRegister 'AND' bRegister
          | bRegister 'AND' aRegister
          | aRegister 'OR' bRegister
          | bRegister 'OR' aRegister
          | 'NOT' aRegister
          | 'NOT' bRegister
          | aRegister '+' bRegister
          | bRegister '+' aRegister
          | aRegister '+' '1'
          | bRegister '+' '1'
          | bRegister '-' aRegister
          | '-' aRegister
          | bRegister '-' '1'
          | aRegister '+' bRegister '+' '1'
          | bRegister '+' aRegister '+' '1'
          | aRegister
          | bRegister
          | '-' '1'
          | '0'
          | '1'
          ;

aRegister : 'H';

bRegister : 'MDR'
          | 'PC'
          | 'MBRU'
          | 'MBR'
          | 'SP'
          | 'LV'
          | 'CPP'
          | 'TOS'
          | 'OPC'
          ;

cRegister : 'MAR'
          | 'MDR'
          | 'PC'
          | 'SP'
          | 'LV'
          | 'CPP'
          | 'TOS'
          | 'OPC'
          | 'H'
          ;

condition : 'N' | 'Z';

memoryStatement : wordMemoryStatement
                | byteMemoryStatement
                | wordMemoryStatement ';' byteMemoryStatement
                | byteMemoryStatement ';' wordMemoryStatement
                ;

wordMemoryStatement : 'rd'
                    | 'wr'
                    ;

byteMemoryStatement : 'fetch';

controlStatement : gotoStatement
                 | ifStatement
                 ;

gotoStatement : 'goto' NAME
              | 'goto' '(' mbrExpr ')'
              ;

mbrExpr : 'MBR' 'OR' ADDRESS
        | 'MBR'
        ;

ifStatement : 'if' '(' condition ')' 'goto' NAME ';' 'else' 'goto' NAME;

EMPTY_STATEMENT : 'empty';
HALT_STATEMENT : 'halt';

NAME : NAME_FIRST_CHAR NAME_CHAR*;
ADDRESS : HEX_PREFIX HEX_DIGIT+;
COMMENT : '#' .*? '\r'? '\n' -> skip;
WHITESPACE : [ \t\r\n]+ -> skip;

fragment NAME_FIRST_CHAR : [a-zA-Z_];
fragment NAME_CHAR : [a-zA-Z0-9_];
fragment HEX_PREFIX : '0x';
fragment HEX_DIGIT : [0-9A-F];
