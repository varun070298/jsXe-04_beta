package org.syntax.jedit.tokenmarker;
/*
 * XMLTokenMarker.java - XML token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 * Copyright (C) 2001 Tom Bradford
 * Portions Copyright Ian Lewis (IanLewis@member.fsf.org)
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

import javax.swing.text.Segment;
import net.sourceforge.jsxe.util.Log;
import org.syntax.jedit.SyntaxUtilities;

/**
 * XML Token Marker Rewrite
 *
 * @author Tom Bradford
 * @author Ian Lewis
 * @version $Id: XMLTokenMarker.java,v 1.4 2006/02/10 02:19:31 ian_lewis Exp $
 */
public class XMLTokenMarker extends TokenMarker {
   public XMLTokenMarker() {
   }
   
   /**
    * Marks Tokens in the buffer. The mapping for XML constructs and token types
    * is as follows.
    * <table>
    *    <tr>
    *       <td>NULL</td><td>Text</td>
    *    </tr>
    *    <tr>
    *       <td>COMMENT1</td><td>Comment</td>
    *    </tr>
    *    <tr>
    *       <td>COMMENT2</td><td>Declaration</td>
    *    </tr>
    *    <tr>
    *       <td>LITERAL1</td><td>Attribute Value started by "</td>
    *    </tr>
    *    <tr>
    *       <tr>LITERAL2</td><td>Attribute Value started by '</td>
    *    </tr>
    *    <tr>
    *       <td>LITERAL3</td><td>CDATA</td>
    *    </tr>
    *    <tr>
    *       <td>LABEL</td><td>Entity Reference</td>
    *    </tr>
    *    <tr>
    *       <td>KEYWORD1</td><td>Element</td>
    *    </tr>
    *    <tr>
    *       <td>KEYWORD2</td><td>Attribute Name</td>
    *    </tr>
    *    <tr>
    *       <td>KEYWORD3</td><td>Processing Instruction</td>
    *    </tr>
    *    <tr>
    *       <td>KEYWORD4</td><td>Namespace prefix</td>
    *    </tr>
    *    <tr>
    *       <td>OPERATOR</td><td>Equals between attribute name and value, quotes, and tag markup</td>
    *    </tr>
    *    <tr>
    *       <td>INVALID</td><td>Anything invalid</td>
    *    </tr>
    */
   public byte markTokensImpl(byte token, Segment line, int lineIndex) {
      char[] array = line.array;
      int offset = line.offset;
      int lastOffset = offset;
      int length = line.count + offset;
      
      // Ugly hack to handle multi-line tags
      boolean sk1 = token == Token.KEYWORD1;
      
      //hack to handle DTD internal subsets and multi-line internal DTD subsets
      //doesn't work right for multi-line internal DTDs with no subset
      boolean internalSubset = token == Token.COMMENT2;
      
      for ( int i = offset; i < length; i++ ) {
         int ip1 = i+1;
         char c = array[i];
        // Log.log(Log.DEBUG,this, String.valueOf(c));
         switch ( token ) {
            case Token.NULL: // text
               switch ( c ) {
                  case '<':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     if ( SyntaxUtilities.regionMatches(false, line, ip1, "!--") ) {
                        i += 3;
                        token = Token.COMMENT1;
                     }
                     else if ( array[ip1] == '!' ) {
                        i += 1;
                        token = Token.COMMENT2;
                     }
                     else if ( array[ip1] == '?' ) {
                        i += 1;
                        token = Token.KEYWORD3;
                     }
                     else
                        token = Token.OPERATOR; // add the < as an operator
                     break;
                     
                  case '&':
                     addToken(i - lastOffset, token);
                     lastOffset = i;
                     token = Token.LABEL;
                     break;
               }
               if ( SyntaxUtilities.regionMatches(false, line, i, "]]>") ) {
                  addToken(i-lastOffset, token);
                  lastOffset = i;
                  addToken((i+3)-lastOffset, Token.INVALID);
                  lastOffset = i+3;
                  //no change in token type
               }
               break;
               
            case Token.KEYWORD1: // tag
               switch ( c ) {
                  case '>':
                    // addToken(ip1-lastOffset, token);
                    // lastOffset = ip1;
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     addToken(ip1-lastOffset, Token.OPERATOR); // add the '>' as an operator
                     lastOffset = ip1;
                     token = Token.NULL;
                     sk1 = false;
                     break;
                     
                  case ' ':
                  case '\t':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     token = Token.KEYWORD2;
                     sk1 = false;
                     break;
                  case ':':
                     addToken(ip1-lastOffset, Token.KEYWORD4);
                     lastOffset = ip1;
                     //no change to the keyword.
                     sk1 = false;
                     break;
                  default:
                     if ( sk1 ) {
                        token = Token.KEYWORD2;
                        sk1 = false;
                     }
                     break;
               }
               break;

            case Token.KEYWORD2: // attribute
               switch ( c ) {
                   case '>':
                     addToken(ip1-lastOffset, token);
                     lastOffset = ip1;
                     token = Token.NULL;
                     break;
                    
                  case '/':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     token = Token.KEYWORD1;
                     break;
                     
                  case '=':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     token = Token.OPERATOR;
               }
               break;
               
            case Token.OPERATOR: // equal for attribute
               switch ( c ) {
                  case '\"':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     addToken(ip1-lastOffset, Token.OPERATOR); //add the quote as on operator
                     lastOffset = ip1;
                     token = Token.LITERAL1;
                     break;
                  case '\'':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     addToken(ip1-lastOffset, Token.OPERATOR); //add the quote as on operator
                     lastOffset = ip1;
                     token = Token.LITERAL2;
                     break;
                  case '>':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     token = Token.NULL;
                     break;
                  case '/':
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     addToken(ip1-lastOffset, Token.OPERATOR); //add the '/' as an operator
                     lastOffset = ip1;
                     token = Token.KEYWORD1;
                     break;
                  case ' ':
                     //don't go to keyword if we have a space
                     break;
                  default:
                     addToken(i-lastOffset, token);
                     lastOffset = i;
                     token = Token.KEYWORD1;
                     break;
               }
               break;
            case Token.LITERAL1: // strings
               if ( c == '\"' ) {
                  addToken(i-lastOffset, token);
                  lastOffset = i;
                  addToken(ip1-lastOffset, Token.OPERATOR); //add the quote as an operator
                  lastOffset = ip1;
                  token = Token.KEYWORD1;
               }
               break;
            case Token.LITERAL2:
               if ( c == '\'' ) {
                  addToken(i-lastOffset, token);
                  lastOffset = i;
                  addToken(ip1-lastOffset, Token.OPERATOR); //add the quote as an operator
                  lastOffset = ip1;
                  token = Token.KEYWORD1;
               }
               break;
            
            case Token.LITERAL3:
               if ( SyntaxUtilities.regionMatches(false, line, i, "]]>") ) {
                  addToken((i+3)-lastOffset, token);
                  lastOffset = i+3;
                  token = Token.NULL;
               }
               break;
            
            case Token.LABEL: // entity
               if ( c == ';' ) {
                  addToken(ip1-lastOffset, token);
                  lastOffset = ip1;
                  token = Token.NULL;
                  break;
               }
               break;
               
            case Token.COMMENT1: // Inside a comment
               if ( SyntaxUtilities.regionMatches(false, line, i, "-->") ) {
                  addToken((i+3)-lastOffset, token);
                  lastOffset = i+3;
                  token = Token.NULL;
               }
               break;

            case Token.COMMENT2: // Inside a declaration
                if ( SyntaxUtilities.regionMatches(false, line, i, "[") ) {
                    internalSubset = true;
                }
                if ( SyntaxUtilities.regionMatches(false, line, i, "]") ) {
                    internalSubset = false;
                }
                if ( SyntaxUtilities.regionMatches(false, line, i, ">") && !internalSubset) {
                    addToken(ip1-lastOffset, token);
                    lastOffset = ip1;
                    token = Token.NULL;
                }
                if (SyntaxUtilities.regionMatches(false, line, i, "[CDATA[") ) {
                    token = Token.LITERAL3;
                }
                break;

            case Token.KEYWORD3: // Inside a processor instruction
               if ( SyntaxUtilities.regionMatches(false, line, i, "?>") ) {
                  addToken((i+2)-lastOffset, token);
                  lastOffset = i+2;
                  token = Token.NULL;
               }
               break;
               
            default:
               throw new InternalError("Invalid state: " + token);
         }
      }
      
      switch ( token ) {
         case Token.LABEL:
            addToken(length-lastOffset, Token.INVALID);
            token = Token.NULL;
            break;
            
         default:
            addToken(length-lastOffset, token);
            break;
      }
      
      return token;
   }
}


