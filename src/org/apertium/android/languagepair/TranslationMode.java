/*
 * Copyright (C) 2012 Arink Verma
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package org.apertium.android.languagepair;

public class TranslationMode {
  //private variables
  public String id = null;
  public String title = null;
  public String packageName = null;
  public String fromLang = null;
  public String toLang = null;

  public TranslationMode(String id, String title) {
    this.id = id;
    this.title = title;
    this.packageName = null;
    String[] s = title.split("[^\\w]+");
    this.fromLang = s[0];
    this.toLang = s[1];
  }

  public boolean isValid() {
    return (this.id != null
        && this.title != null
        && this.packageName != null
        && this.fromLang != null
        && this.toLang != null);
  }
}
