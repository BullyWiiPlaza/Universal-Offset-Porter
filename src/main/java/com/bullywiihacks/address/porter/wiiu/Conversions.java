package com.bullywiihacks.address.porter.wiiu;

import lombok.val;
import lombok.var;

import java.util.List;

import static java.lang.Long.toHexString;
import static java.lang.String.format;

class Conversions
{
	static String byteArrayToHexadecimal(byte[] byteArray)
	{
		val capacity = byteArray.length * 2;
		val stringBuilder = new StringBuilder(capacity);
		for (val singleByte : byteArray)
		{
			val format = format("%02x ", singleByte).toUpperCase();
			stringBuilder.append(format);
		}

		return stringBuilder.toString().trim();
	}

	static String numberToHexadecimal(long value)
	{
		val stringBuilder = new StringBuilder();
		if (value < 0)
		{
			stringBuilder.append("-");
			value *= -1;
		}

		stringBuilder.append("0x");
		stringBuilder.append(toHexString(value).toUpperCase());
		return stringBuilder.toString();
	}

	static byte[] toByteArray(List<Byte> bytes)
	{
		val byteArray = new byte[bytes.size()];
		for (var index = 0; index < bytes.size(); index++)
		{
			byteArray[index] = bytes.get(index);
		}

		return byteArray;
	}
}
