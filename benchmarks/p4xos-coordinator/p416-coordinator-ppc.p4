#include <core.p4>
#include <v1model.p4>

header arp_t {
    bit<16> hrd;
    bit<16> pro;
    bit<8>  hln;
    bit<8>  pln;
    bit<16> op;
    bit<48> sha;
    bit<32> spa;
    bit<48> tha;
    bit<32> tpa;
}

header ethernet_t {
    bit<48> dstAddr;
    bit<48> srcAddr;
    bit<16> etherType;
}

header igmp_t {
    bit<8>  msgtype;
    bit<8>  max_resp;
    bit<16> checksum;
    bit<32> grp_addr;
}

header ipv4_t {
    bit<4>  version;
    bit<4>  ihl;
    bit<8>  diffserv;
    bit<16> totalLen;
    bit<16> identification;
    bit<3>  flags;
    bit<13> fragOffset;
    bit<8>  ttl;
    bit<8>  protocol;
    bit<16> hdrChecksum;
    bit<32> srcAddr;
    bit<32> dstAddr;
}

header ipv6_t {
    bit<4>   version;
    bit<8>   trafficClass;
    bit<20>  flowLabel;
    bit<16>  payloadLen;
    bit<8>   nextHdr;
    bit<8>   hopLimit;
    bit<128> srcAddr;
    bit<128> dstAddr;
}

header paxos_t {
    bit<16>  msgtype;
    bit<32>  inst;
    bit<16>  rnd;
    bit<16>  vrnd;
    bit<16>  acptid;
    bit<32>  paxoslen;
    bit<256> paxosval;
}

header udp_t {
    bit<16> srcPort;
    bit<16> dstPort;
    bit<16> length_;
    bit<16> checksum;
}

struct metadata {
}

struct headers {
    @name(".arp") 
    arp_t      arp;
    @name(".ethernet") 
    ethernet_t ethernet;
    @name(".igmp") 
    igmp_t     igmp;
    @name(".ipv4") 
    ipv4_t     ipv4;
    @name(".ipv6") 
    ipv6_t     ipv6;
    @name(".paxos") 
    paxos_t    paxos;
    @name(".udp") 
    udp_t      udp;
}

parser ParserImpl(packet_in packet, out headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".parse_arp") state parse_arp {
        packet.extract(hdr.arp);
        transition accept;
    }
    @name(".parse_ethernet") state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            16w0x806: parse_arp;
            16w0x800: parse_ipv4;
            16w0x86dd: parse_ipv6;
            default: accept;
        }
    }
    @name(".parse_ipv4") state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition select(hdr.ipv4.protocol) {
            8w0x11: parse_udp;
            default: accept;
        }
    }
    @name(".parse_ipv6") state parse_ipv6 {
        packet.extract(hdr.ipv6);
        transition accept;
    }
    @name(".parse_paxos") state parse_paxos {
        packet.extract(hdr.paxos);
        transition accept;
    }
    @name(".parse_udp") state parse_udp {
        packet.extract(hdr.udp);
        transition select(hdr.udp.dstPort) {
            16w0x8888: parse_paxos;
            16w0x8889: parse_paxos;
            default: accept;
        }
    }
    @name(".start") state start {
        transition parse_ethernet;
    }
}

control egress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    apply {
    }
}

@name(".instance_register") register<bit<32>>(32w1) instance_register;

control ingress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".forward") action forward(bit<9> port) {
        standard_metadata.egress_spec = port;
    }
    @name("._drop") action _drop() {
        mark_to_drop();
    }
    @name(".increase_instance") action increase_instance() {
        instance_register.read(hdr.paxos.inst, (bit<32>)0);
        hdr.paxos.inst = hdr.paxos.inst + 32w1;
        instance_register.write((bit<32>)0, (bit<32>)hdr.paxos.inst);
        hdr.udp.dstPort = 16w0x8889;
        hdr.udp.checksum = 16w0;
    }
    @name("._nop") action _nop() {
    }
    @name(".forward_tbl") table forward_tbl {
        actions = {
            forward;
            _drop;
        }
        key = {
            standard_metadata.ingress_port: exact;
        }
        size = 48;
    }
    @name(".sequence_tbl") table sequence_tbl {
        actions = {
            increase_instance;
            _nop;
        }
        key = {
            hdr.paxos.msgtype: exact;
        }
        size = 1;
    }
    apply {
        if (hdr.ipv4.isValid()) {
            forward_tbl.apply();
        }
        if (hdr.paxos.isValid()) {
            sequence_tbl.apply();
        }
    }
}

control DeparserImpl(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv6);
        packet.emit(hdr.ipv4);
        packet.emit(hdr.udp);
        packet.emit(hdr.paxos);
        packet.emit(hdr.arp);
    }
}

control verifyChecksum(inout headers hdr, inout metadata meta) {
    apply {
        verify_checksum(true, { hdr.ipv4.version, hdr.ipv4.ihl, hdr.ipv4.diffserv, hdr.ipv4.totalLen, hdr.ipv4.identification, hdr.ipv4.flags, hdr.ipv4.fragOffset, hdr.ipv4.ttl, hdr.ipv4.protocol, hdr.ipv4.srcAddr, hdr.ipv4.dstAddr }, hdr.ipv4.hdrChecksum, HashAlgorithm.csum16);
    }
}

control computeChecksum(inout headers hdr, inout metadata meta) {
    apply {
        update_checksum(true, { hdr.ipv4.version, hdr.ipv4.ihl, hdr.ipv4.diffserv, hdr.ipv4.totalLen, hdr.ipv4.identification, hdr.ipv4.flags, hdr.ipv4.fragOffset, hdr.ipv4.ttl, hdr.ipv4.protocol, hdr.ipv4.srcAddr, hdr.ipv4.dstAddr }, hdr.ipv4.hdrChecksum, HashAlgorithm.csum16);
    }
}

V1Switch(ParserImpl(), verifyChecksum(), ingress(), egress(), computeChecksum(), DeparserImpl()) main;

