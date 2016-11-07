package com.bullywiihacks.address.porter.wii;

public class CodeAddress
{
	private int address;
	private boolean isAssembly;

	public CodeAddress(int address, boolean isAssembly)
	{
		this.setAddress(address);
		this.setAssembly(isAssembly);
	}

	public boolean isAssembly()
	{
		return isAssembly;
	}

	public void setAssembly(boolean isAssembly)
	{
		this.isAssembly = isAssembly;
	}

	public int getAddress()
	{
		return address;
	}

	public void setAddress(int address)
	{
		this.address = address;
	}
}