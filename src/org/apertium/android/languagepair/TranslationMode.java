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
  private String _id = null;
  private String _title = null;
  private String _package = null;
  private String _from = null;
  private String _to = null;

  public TranslationMode(String id, String title) {
    this._id = id;
    this._title = title;
    this._package = null;
    String[] s = title.split("[^\\w]+");
    this._from = s[0];
    this._to = s[1];
  }

  public String getID() {
    return this._id;
  }

  public String getTitle() {
    return this._title;
  }

  public void setTo(String i) {
    this._to = i;
  }

  public String getTo() {
    return this._to;
  }

  public void setFrom(String i) {
    this._from = i;
  }

  public String getFrom() {
    return this._from;
  }

  public void setPackage(String i) {
    this._package = i;
  }

  public String getPackage() {
    return this._package;
  }

  public boolean isValid() {
    return (this._id != null
        && this._title != null
        && this._package != null
        && this._from != null
        && this._to != null);
  }
}
