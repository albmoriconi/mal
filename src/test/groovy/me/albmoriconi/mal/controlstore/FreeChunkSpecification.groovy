package me.albmoriconi.mal.controlstore

import spock.lang.*

class FreeChunkSpecification extends Specification {

    @Unroll
    def "creating a free chunk from #startAddress to #endAddress"() {
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

    def "creating an invalid free chunk"() {
        when:
        new FreeChunk(38, 2)

        then:
        thrown IllegalArgumentException
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
