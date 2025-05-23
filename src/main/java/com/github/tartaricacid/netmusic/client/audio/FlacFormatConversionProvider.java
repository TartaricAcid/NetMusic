/*
 * libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package com.github.tartaricacid.netmusic.client.audio;

import org.jflac.sound.spi.Flac2PcmAudioInputStream;
import org.jflac.sound.spi.FlacEncoding;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.spi.FormatConversionProvider;

/**
 * A format conversion provider provides format conversion services from one or
 * more input formats to one or more output formats. Converters include codecs,
 * which encode and/or decode audio data, as well as transcoders, etc. Format
 * converters provide methods for determining what conversions are supported and
 * for obtaining an audio stream from which converted data can be read. The
 * source format represents the format of the incoming audio data, which will be
 * converted. The target format represents the format of the processed,
 * converted audio data. This is the format of the data that can be read from
 * the stream returned by one of the getAudioInputStream methods.
 *
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @author Florian Bomers
 * @version $Revision: 1.1 $
 */
public class FlacFormatConversionProvider extends FormatConversionProvider {
    private static final boolean DEBUG = false;
    private static final boolean HAS_ENCODING = false;

    public FlacFormatConversionProvider() {
    }

    public AudioFormat.Encoding[] getSourceEncodings() {
        AudioFormat.Encoding[] encodings = new AudioFormat.Encoding[]{FlacEncoding.FLAC};
        return encodings;
    }

    public AudioFormat.Encoding[] getTargetEncodings() {
        AudioFormat.Encoding[] encodings = new AudioFormat.Encoding[]{FlacEncoding.PCM_SIGNED};
        return encodings;
    }

    private boolean isBitSizeOK(AudioFormat format, boolean notSpecifiedOK) {
        int bitSize = format.getSampleSizeInBits();
        return notSpecifiedOK && bitSize == -1 || bitSize == 8 || bitSize == 16 || bitSize == 24;
    }

    private boolean isChannelsOK(AudioFormat format, boolean notSpecifiedOK) {
        int channels = format.getChannels();
        return notSpecifiedOK && channels == -1 || channels == 1 || channels == 2;
    }

    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
        boolean bitSizeOK = this.isBitSizeOK(sourceFormat, true);
        boolean channelsOK = this.isChannelsOK(sourceFormat, true);
        AudioFormat.Encoding[] encodings;
        if (bitSizeOK && channelsOK && sourceFormat.getEncoding().equals(FlacEncoding.FLAC)) {
            encodings = new AudioFormat.Encoding[]{Encoding.PCM_SIGNED};
            return encodings;
        } else {
            encodings = new AudioFormat.Encoding[0];
            return encodings;
        }
    }

    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return this.getTargetFormats(targetEncoding, sourceFormat, true);
    }

    private AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat, boolean notSpecifiedOK) {
        boolean bitSizeOK = this.isBitSizeOK(sourceFormat, notSpecifiedOK);
        boolean channelsOK = this.isChannelsOK(sourceFormat, notSpecifiedOK);
        AudioFormat[] formats;
        if (bitSizeOK && channelsOK && sourceFormat.getEncoding().equals(FlacEncoding.FLAC) && targetEncoding.equals(Encoding.PCM_SIGNED)) {
            formats = new AudioFormat[]{new AudioFormat(sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), sourceFormat.getChannels(), true, false)};
            return formats;
        } else {
            formats = new AudioFormat[0];
            return formats;
        }
    }

    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
        AudioFormat[] formats = this.getTargetFormats(targetEncoding, sourceStream.getFormat(), false);
        if (formats != null && formats.length > 0) {
            return this.getAudioInputStream(formats[0], sourceStream);
        } else {
            throw new IllegalArgumentException("conversion not supported");
        }
    }

    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        AudioFormat[] formats = this.getTargetFormats(targetFormat.getEncoding(), sourceFormat, false);
        if (formats != null && formats.length > 0) {
            if (sourceFormat.equals(targetFormat)) {
                return sourceStream;
            } else if (sourceFormat.getChannels() == targetFormat.getChannels() && sourceFormat.getSampleSizeInBits() == targetFormat.getSampleSizeInBits() && !targetFormat.isBigEndian() && sourceFormat.getEncoding().equals(FlacEncoding.FLAC) && targetFormat.getEncoding().equals(Encoding.PCM_SIGNED)) {
                return new Flac2PcmAudioInputStream(sourceStream, targetFormat, -1L);
            } else if (sourceFormat.getChannels() == targetFormat.getChannels() && sourceFormat.getSampleSizeInBits() == targetFormat.getSampleSizeInBits() && sourceFormat.getEncoding().equals(Encoding.PCM_SIGNED) && targetFormat.getEncoding().equals(FlacEncoding.FLAC)) {
                throw new IllegalArgumentException("FLAC encoder not yet implemented");
            } else {
                throw new IllegalArgumentException("unable to convert " + sourceFormat.toString() + " to " + targetFormat.toString());
            }
        } else {
            throw new IllegalArgumentException("conversion not supported");
        }
    }
}
