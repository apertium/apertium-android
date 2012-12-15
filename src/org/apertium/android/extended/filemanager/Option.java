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

/* used by FileArray */
package org.apertium.android.extended.filemanager;

class Option implements Comparable<Option> {
  private String name;
  private String data;
  private String path;

  public Option(String n, String d, String p) {
    name = n;
    data = d;
    path = p;
  }

  public String getName() {
    return name;
  }

  public String getData() {
    return data;
  }

  public String getPath() {
    return path;
  }

  @Override
  public int compareTo(Option o) {
    if (this.name != null) {
      return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
    } else {
      throw new IllegalArgumentException();
    }
  }
}
