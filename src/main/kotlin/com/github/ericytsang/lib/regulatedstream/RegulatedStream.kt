package com.github.ericytsang.lib.regulatedstream

import com.github.ericytsang.lib.abstractstream.AbstractOutputStream
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue

class RegulatedOutputStream(val stream:OutputStream):AbstractOutputStream()
{
    private val permits = LinkedBlockingQueue<Int>()

    private var currentPermit = 0

    /**
     * allow the output stream to write [numBytes] more bytes in addition to
     * previous calls to permit.
     */
    fun permit(numBytes:Int)
    {
        if (numBytes < 0)
        {
            throw IllegalArgumentException("argument must be > 0: $numBytes")
        }
        permits.put(numBytes)
    }

    override fun doClose()
    {
        stream.close()
        doNothing()
    }

    override fun flush() = stream.flush()

    override fun doWrite(b:ByteArray,off:Int,len:Int)
    {
        var cursor = off
        var remainingLen = len
        while (remainingLen > 0)
        {
            while (currentPermit == 0)
            {
                currentPermit = permits.take()
            }
            val bytesToTransmit = Math.min(currentPermit,remainingLen)
            stream.write(b,cursor,bytesToTransmit)
            currentPermit -= bytesToTransmit
            remainingLen -= bytesToTransmit
            cursor += bytesToTransmit
        }
    }
}

