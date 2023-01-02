/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.eclipse.tools.micro;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *
 * @author Gaurav Gupta
 */
public class PayaraMicroPlugin extends AbstractUIPlugin {

	private static PayaraMicroPlugin instance;

	public PayaraMicroPlugin() {
		instance = this;
	}

	public static final PayaraMicroPlugin getInstance() {
		return instance;
	}
}
