        .feature at_in_identifiers
        .feature dollar_in_identifiers
	.import parameterVal0
	.import parameterVal1
	.import parameterVal2
	.import returnVal
	.export _func@fooutil@@putCh
	.export _func@fooutil@@putInt
	.export _func@fooutil@@doNothing
	.export _func@fooutil@@toChar
	.export _func@fooutil@@graphicsOn
	.export _func@fooutil@@screenOn
	.export _func@fooutil@@screenOff
	.export _func@fooutil@@setBorderColor
	.export _func@fooutil@@setPixel
	.export _func@fooutil@@clearPixel

.segment	"ZEROPAGE"
clearPtr:	.addr 0

; Temporary variables for operations
op1:	.addr 0
op2:	.addr 0
out:	.addr 0

.segment	"DATA"

; For __findpoint
_fpx:	.addr 0
_fpy:	.byte 0
_fptemp:	.addr 0

.segment        "CODE"

.proc   _func@fooutil@@putCh
	lda parameterVal0
	jsr $FFD2
	rts
.endproc

.proc   _func@fooutil@@putInt
	lda parameterVal0
	adc #48
	clc
	clv
	jsr $FFD2
	rts
.endproc

.proc   _func@fooutil@@doNothing
	lda #2
	;jsr $FFD2
	rts
.endproc

.proc	_func@fooutil@@graphicsOn
	; Place graphics memory
	lda 53272
	ora #8
	sta 53272

	; VIC control register OR 32 (turn on graphics mode)
	lda 53265
	ora #32
	sta 53265
	
	jsr _func@fooutil@@clearScreen
	
	rts
.endproc

.proc	_func@fooutil@@screenOff
	lda 53265
	and #239
	sta 53265
	rts
.endproc

.proc	_func@fooutil@@screenOn
	lda 53265
	ora #16
	sta 53265
	rts
.endproc

.proc	_func@fooutil@@toChar
	lda parameterVal0
	rts
.endproc

.proc	_func@fooutil@@setBorderColor
	lda parameterVal0
	sta 53248+32
	rts
.endproc

.proc	_func@fooutil@@clearScreen
	ldy #$ff
	lda #32
	sta clearPtr+1
	lda #$00
	sta clearPtr
clsStart:	
	sta (clearPtr),Y
	dey
	bne clsStart
	lda clearPtr+1
	cmp #63       ; Stop value for high-order bit (starts at 32)
	beq clsEnd
	inc clearPtr+1
	lda #$00
	jmp clsStart
clsEnd:
	; TEMP: shove in some 'on' pixels for comparison
	lda #$ff
	sta $2000+32
	rts
.endproc

.proc	__divbyeight ; Quick divide by eight by bit-shifting
	lsr A
	lsr A
	lsr A
	rts
.endproc

.proc	__multbyeight ; Quick times by eight by bit-shifting
	asl A
	asl A
	asl A
	rts
.endproc

.proc	__mult16   ; 16-bit multiplication    op1 * op2 -> [op1,out] (32-bit result, out high)
                   ; From mult-div.s on "The Fridge"
	lda #0
	sta out+1
	ldy #$11
	clc
loop:	ror out+1
	ror
	ror op1+1
	bcc end
	clc
	adc op2
	pha
	lda op2+1
	adc out+1
	sta out+1
	pla
end:	dey
	bne loop
	sta out
	rts

.endproc

.proc	__findpoint
	ldx _fpx
	ldy _fpy
	
	;; Calculating row address to _fptemp
	;  y / 8 -> a
	tya
	jsr __divbyeight
	
	; a * 320 -> op1
	sta <op1
	lda #0
	sta >op1
	lda #1
	sta >op2
	lda #64
	sta <op2
	jsr __mult16
	
	; op1 + _fpy & 7 -> op1
	clc

	lda _fpy
	and #$07
	adc <op1
	; now handle carry
	bcc nocarry1
	inc >op1
nocarry1:

	; op1 -> _fptemp (need op1!)
	lda <op1
	sta <_fptemp
	lda >op1
	sta >_fptemp
	
	;; Now calculating column/row(in-block) address offset
	; Just mask out the lowest three bits. They will be represented
	; in the mask.
	lda <_fpx
	and #$f8
	
	; Add the previously-computed row offset to get the final byte offset
	; in _fpy  (<_fpx & $f8 already in accumulator)
	clc
	adc <_fptemp
	sta <_fpy
	lda >_fpx
	adc >_fptemp  ; Handles carry if any
	clc
	adc #$20      ; Add the graphics memory offset ($2000, only affects high-byte)
	sta >_fpy

	;; Now calculate the bitmask for the addressed byte
	; First get the bit to set (7 - (_fpx & 7))
	lda <_fpx    ; Can ignore high-order byte since it'd become zero anyway
	and #$07
	sta <_fpx
	lda #$07
	clc
	sbc <_fpx
	

        ; FIXME: accumulator currently contains which bit needs to be on.
        ;    Should be an actual mask with that bit turned on for ORing

	; Mask goes in accumulator so that it can be ORed or ANDed with
	; the returned address (in _fpy) directly without further copying
	
	rts
.endproc

.proc	_func@fooutil@@setPixel
	; setPixel(int lowX, int highX, int Y)
	lda parameterVal0
	sta <_fpx
	lda parameterVal1
	sta >_fpx
	lda parameterVal2
	sta _fpy
	
	jsr __findpoint   ; Mask in accumulator, address in _fpy
	ora _fpy
	sta _fpy
	rts
.endproc

.proc	_func@fooutil@@clearPixel
	; clearPixel(int lowX, int highX, int Y)
	lda parameterVal0
	sta <_fpx
	lda parameterVal1
	sta >_fpx
	lda parameterVal2
	sta _fpy
	
	jsr __findpoint   ; Mask in accumulator, address in _fpy
	eor #$ff   ; Bitwise Complement
	and _fpy
	sta _fpy
	rts
.endproc

