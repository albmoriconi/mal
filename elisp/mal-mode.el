;;; mal-mode.el --- mode for editing MIC-1 Micro Assembly Language

;; Copyright (C) 2019 Alberto Moriconi

;; Author: Alberto Moriconi
;; Keywords: tools, languages

;; This program is free software: you can redistribute it and/or modify it under
;; the terms of the GNU General Public License as published by the Free Software
;; Foundation, either version 3 of the License, or (at your option) any later
;; version.

;; This program is distributed in the hope that it will be useful, but WITHOUT
;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
;; FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
;; details.

;; You should have received a copy of the GNU General Public License along with
;; this program. If not, see <http://www.gnu.org/licenses/>.

;;; Code:

(defgroup mal nil
  "Mode for editing MIC-1 Micro Assembly Language."
  :link '(custom-group-link :tag "Font Lock Faces group" font-lock-faces)
  :group 'languages)

(defvar mal-mode-syntax-table
  (let ((st (make-syntax-table)))
    ;; # begins comments
    (modify-syntax-entry ?\# "<" st)
    ;; Newline ends comments
    (modify-syntax-entry ?\n ">" st)
    ;; Separator for parts of instruction
    (modify-syntax-entry ?\; "." st)
    ;; Plus sign
    (modify-syntax-entry ?+ "." st)
    ;; Minus sign
    (modify-syntax-entry ?- "." st)
    ;; Equal sign
    (modify-syntax-entry ?= "." st)
    ;; Less-than sign
    (modify-syntax-entry ?< "." st)
    st)
  "Syntax table used while in MAL mode.")

(defconst mal-font-lock-keywords
  (append
   '(
     ;; Labels
     ("^\\(\\(\\sw\\|\\s_\\)+\\)\\>:?[ \t]*" (1 font-lock-type-face))
     ;; Register names
     ("\\_<\\(MAR\\|MDR\\|PC\\|MBRU\\|MBR\\|SP\\|LV\\|CPP\\|TOS\\|OPC\\|H\\)\\_>" .
      font-lock-variable-name-face)
     ;; Flags
     ("\\_<\\(Z\\|N\\)\\_>" . font-lock-variable-name-face)
     ;; Memory signals
     ("\\_<\\(rd\\|wr\\|fetch\\)\\_>" . font-lock-function-name-face)
     ;; Keywords
     ("\\_<\\(if\\|else\\|goto\\|halt\\|empty\\)\\_>" . font-lock-keyword-face)
     )
   cpp-font-lock-keywords)
  "Additional expressions to highlight in MAL mode.")

;;;###autoload
(add-to-list 'auto-mode-alist '("\\.mal\\'" . mal-mode))

(define-derived-mode mal-mode prog-mode "MIC-1 MAL"
  "Major mode for editing MIC-1 Micro Assembly Language."
  (set (make-local-variable 'font-lock-defaults) '(mal-font-lock-keywords))
  (set-syntax-table (make-syntax-table mal-mode-syntax-table)))

(provide 'mal-mode)

;;; mal-mode.el ends here
