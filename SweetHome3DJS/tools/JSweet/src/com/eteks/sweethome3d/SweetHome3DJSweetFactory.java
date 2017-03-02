/*
 * SweetHome3DJSweetFactory.java 
 *
 * Sweet Home 3D, Copyright (c) 2017 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d;

import org.jsweet.transpiler.JSweetContext;
import org.jsweet.transpiler.JSweetFactory;
import org.jsweet.transpiler.util.PrinterAdapter;

/**
 * JSweet extension to generate JavaScript code from the SweetHome3D Java code
 * base.
 * 
 * @author Renaud Pawlak
 */
public class SweetHome3DJSweetFactory extends JSweetFactory {

  public SweetHome3DJSweetFactory() {
  }

  public PrinterAdapter createAdapter(JSweetContext context) {
    return new SweetHome3DJava3DJSweetAdapter(new SweetHome3DJSweetAdapter(super.createAdapter(context)));
  }

}
