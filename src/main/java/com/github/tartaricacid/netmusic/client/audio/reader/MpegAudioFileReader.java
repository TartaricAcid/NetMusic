/*
 * MpegAudioFileReader.
 *
 * 10/10/05 : size computation bug fixed in parseID3v2Frames.
 *            RIFF/MP3 header support added.
 *            FLAC and MAC headers throw UnsupportedAudioFileException now.
 *            "mp3.id3tag.publisher" (TPUB/TPB) added.
 *            "mp3.id3tag.orchestra" (TPE2/TP2) added.
 *            "mp3.id3tag.length" (TLEN/TLE) added.
 *
 * 08/15/05 : parseID3v2Frames improved.
 *
 * 12/31/04 : mp3spi.weak system property added to skip controls.
 *
 * 11/29/04 : ID3v2.2, v2.3 & v2.4 support improved.
 *            "mp3.id3tag.composer" (TCOM/TCM) added
 *            "mp3.id3tag.grouping" (TIT1/TT1) added
 *            "mp3.id3tag.disc" (TPA/TPOS) added
 *            "mp3.id3tag.encoded" (TEN/TENC) added
 *            "mp3.id3tag.v2.version" added
 *
 * 11/28/04 : String encoding bug fix in chopSubstring method.
 *
 * JavaZOOM : mp3spi@javazoom.net
 * 			  http://www.javazoom.net
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package com.github.tartaricacid.netmusic.client.audio.reader;

public class MpegAudioFileReader extends javazoom.spi.mpeg.sampled.file.MpegAudioFileReader {
}