/*
 * Copyright (C) 2019 Alberto Moriconi
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

// Grammar for MIC-1 Micro Assembly Language

grammar Mal;

uProgram : instruction*;

instruction : label? statementSequence;

label : NAME ('=' ADDRESS)? ':';

statementSequence : EMPTY_STATEMENT
                  | haltStatement
                  | assignmentStatement (';' memoryStatement)? (';' controlStatement)?
                  | memoryStatement (';' controlStatement)?
                  | controlStatement
                  ;

assignmentStatement : destination '=' expression;

destination : cRegister
            | condition
            ;

expression : assignmentStatement                #assignmentExpression
           | operation '<<' '8'                 #sll8Expression
           | operation '>>' '1'                 #sra1Expression
           | operation                          #operationExpression
           ;

operation : aRegister 'AND' bRegister           #andOperation
          | bRegister 'AND' aRegister           #andOperation
          | aRegister 'OR' bRegister            #orOperation
          | bRegister 'OR' aRegister            #orOperation
          | 'NOT' aRegister                     #aNotOperation
          | 'NOT' bRegister                     #bNotOperation
          | aRegister '+' bRegister             #sumOperation
          | bRegister '+' aRegister             #sumOperation
          | aRegister '+' '1'                   #aIncOperation
          | bRegister '+' '1'                   #bIncOperation
          | bRegister '-' aRegister             #subOperation
          | '-' aRegister                       #aNegOperation
          | bRegister '-' '1'                   #bDecOperation
          | aRegister '+' bRegister '+' '1'     #sumIncOperation
          | bRegister '+' aRegister '+' '1'     #sumIncOperation
          | aRegister                           #aPassOperation
          | bRegister                           #bPassOperation
          | '-' '1'                             #negOneOperation
          | '0'                                 #zeroOperation
          | '1'                                 #oneOperation
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
                 | gotoMbrExprStatement
                 | ifStatement
                 ;

gotoStatement : 'goto' NAME;

gotoMbrExprStatement : 'goto' '(' mbrExpr ')';

mbrExpr : 'MBR' 'OR' ADDRESS
        | 'MBR'
        ;

ifStatement : 'if' '(' condition ')' 'goto' NAME ';' 'else' 'goto' NAME;

haltStatement : HALT_STATEMENT;

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
