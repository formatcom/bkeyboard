/*
   Creado por Vinicio Valbuena
   Arduino Leonardo


   REF: https://www.arduino.cc/en/Reference/HID
   REF: https://github.com/NicoHood/HID
   REF: https://github.com/NicoHood/HID/blob/master/examples/Keyboard/BootKeyboard/BootKeyboard.ino

   $ sudo rfcomm connect 0 98:D3:41:F5:D9:D1

   -- MODO DEBUG BLUETOOTH

   $ sudo picocom -b 9600 --omap crcrlf --imap spchex /dev/ttyUSB0
   $ sudo picocom -b 9600 --omap crcrlf --imap spchex /dev/rfcomm0

   -- MODO 2 AT

   $ sudo picocom -b 38400 -c --omap crcrlf /dev/ttyUSB0

 */

#include "HID-Project.h"

#define BTSerial       Serial1


void setup()
{
	BTSerial.begin(9600);
	BootKeyboard.begin();
}

void sendKey(uint8_t key)
{
	BootKeyboard.press(key);
	delay(100); // required on nintendo switch
	BootKeyboard.release(key);
}

uint8_t decodeASCII(uint8_t key)
{
	if (!(BootKeyboard.getLeds() & LED_CAPS_LOCK)) return key;

	// pasar a minuscula usando la tabla ascii
	if (key >= 'A' && key <= 'Z') return key + 32;

	// pasar a mayuscula
	if (key >= 'a' && key <= 'z') return key - 32;

	return key;
}

void loop()
{
	if (BTSerial.available())
	{
		sendKey(
			decodeASCII(BTSerial.read())
		);
	}
}
