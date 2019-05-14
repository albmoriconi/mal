/*
 * Copyright (C) 2019 Alberto Moriconi
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.albmoriconi.mal.controlstore

import spock.lang.*

class FreeChunkSpecification extends Specification {

    @Unroll
    def "creating a free chunk (#startAddress, #endAddress)"() {
        given:
        def fc1 = new FreeChunk(startAddress, endAddress)

        expect:
        fc1.getStartAddress() == startAddress
        fc1.getEndAddress() == endAddress

        where:
        startAddress | endAddress
        0            | 0
        0            | 25
        28           | 36
    }

    @Unroll
    def "creating an invalid free chunk (#startAddress, #endAddress)"() {
        when:
        new FreeChunk(startAddress, endAddress)

        then:
        thrown IllegalArgumentException

        where:
        startAddress | endAddress
        0            | -5
        8            | -5
        -6           | -10
        -12          | -4
        5            | 2
        5            | 0
    }

    @Unroll
    def "checking if (#chunkStart, #chunkEnd) contains (#startAddress, #endAddress)"() {
        given:
        def fc1 = new FreeChunk(chunkStart, chunkEnd)

        expect:
        fc1.contains(startAddress, endAddress) == expected

        where:
        chunkStart | chunkEnd | startAddress | endAddress | expected
        0          | 0        | 0            | 0          | true
        0          | 0        | 9            | 21         | false
        0          | 0        | 21           | 9          | false
        15         | 25       | 15           | 25         | true
        15         | 25       | 25           | 25         | true
        15         | 25       | 15           | 15         | true
        15         | 25       | 18           | 21         | true
        15         | 25       | 18           | 36         | false
        15         | 25       | 12           | 21         | false
        15         | 25       | 12           | 28         | false
    }

    @Unroll
    def "checking if (#chunkStart, #chunkEnd) is (#startAddress, #endAddress)"() {
        given:
        def fc1 = new FreeChunk(chunkStart, chunkEnd)

        expect:
        fc1.is(startAddress, endAddress) == expected

        where:
        chunkStart | chunkEnd | startAddress | endAddress | expected
        0          | 0        | 0            | 0          | true
        0          | 0        | 9            | 21         | false
        0          | 0        | 21           | 9          | false
        15         | 25       | 15           | 25         | true
        15         | 25       | 25           | 25         | false
        15         | 25       | 15           | 15         | false
        15         | 25       | 18           | 21         | false
        15         | 25       | 18           | 36         | false
        15         | 25       | 12           | 21         | false
        15         | 25       | 12           | 28         | false
    }
}
