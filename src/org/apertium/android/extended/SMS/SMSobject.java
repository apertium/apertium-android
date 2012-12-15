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
package org.apertium.android.extended.SMS;

public class SMSobject implements Comparable<SMSobject> {
  private String body;
  private String sender;
  private Long date;

  public SMSobject(String b, String s, Long d) {
    body = b;
    sender = s;
    date = d;
  }

  public String getBody() {
    return body;
  }

  public String getSender() {
    return sender;
  }

  public Long getDate() {
    return date;
  }

  @Override
  public int compareTo(SMSobject o) {
    if (this.body != null) {
      return this.date.compareTo(o.getDate()); //this.body.toLowerCase().compareTo(o.getBody().toLowerCase());
    } else {
      throw new IllegalArgumentException();
    }
  }
}
