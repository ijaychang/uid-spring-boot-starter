
package com.zcckj.uid;

import com.zcckj.uid.exception.UidGenerateException;

/**
 * Represents a unique id generator.
 *
 * @author fsren
 */
public interface UidGenerator {

    /**
     * Get a unique ID
     *
     * @return UID
     * @throws UidGenerateException
     */
    long getUID() throws UidGenerateException;

    /**
     * Parse the UID into elements which are used to generate the UID. <br>
     * Such as timestamp & workerId & sequence...
     *
     * @param uid
     * @return Parsed info
     */
    String parseUID(long uid);

    /**
     * Get a history unique ID
     *
     * @return UID
     * @throws UidGenerateException
     */
    long getHistoryUID(long historyTimestamp) throws UidGenerateException;

    /***
     * 获取截止的id(即sequence值最效)
     *
     * @param historyTimeMillis
     * @return long
     */

    long getHistoryUIDOfBeginSequence(long historyTimeMillis);

    /***
     * 获取截止的id(即sequence值最大)
     *
     * @param historyTimeMillis
     * @return long
     */
    long getEndHistoryUIDOfEndSequence(long historyTimeMillis);
}
