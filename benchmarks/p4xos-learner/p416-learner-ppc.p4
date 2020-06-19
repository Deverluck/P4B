#include <core.p4>
#include <v1model.p4>

struct ingress_metadata_t {
    bit<16> round;
    bit<1>  set_drop;
    bit<8>  count;
    bit<8>  acceptors;
    bit<8>  tmp_8;
}

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
    bit<16>  tmp;
}

header udp_t {
    bit<16> srcPort;
    bit<16> dstPort;
    bit<16> length_;
    bit<16> checksum;
}

struct metadata {
    @name(".local_metadata") 
    ingress_metadata_t local_metadata;
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
    @name("._drop") action _drop() {
        mark_to_drop();
    }
    @name("._nop") action _nop() {
    }
    @name(".drop_tbl") table drop_tbl {
        actions = {
            _drop;
            _nop;
        }
        key = {
            meta.local_metadata.set_drop: exact;
        }
        size = 2;
    }
    apply {
        drop_tbl.apply();
    }
}

@name(".history2B") register<bit<8>>(32w65536) history2B;

@name(".rounds_register") register<bit<16>>(32w65536) rounds_register;

@name(".values_register") register<bit<256>>(32w65536) values_register;

control ingress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".forward") action forward(bit<9> port) {
        standard_metadata.egress_spec = port;
        meta.local_metadata.set_drop = 1w0;
    }
    @name("._drop") action _drop() {
        mark_to_drop();
    }
    @name(".handle_2b") action handle_2b() {
        rounds_register.write((bit<32>)hdr.paxos.inst, (bit<16>)hdr.paxos.rnd);
        values_register.write((bit<32>)hdr.paxos.inst, (bit<256>)hdr.paxos.paxosval);
        hdr.paxos.tmp = 16w1 << hdr.paxos.acptid;
        meta.local_metadata.tmp_8 = (bit<8>)hdr.paxos.tmp;
        meta.local_metadata.acceptors = meta.local_metadata.tmp_8 | meta.local_metadata.acceptors;
        history2B.write((bit<32>)hdr.paxos.inst, (bit<8>)meta.local_metadata.acceptors);
    }
    @name(".handle_new_value") action handle_new_value() {
        rounds_register.write((bit<32>)hdr.paxos.inst, (bit<16>)hdr.paxos.rnd);
        values_register.write((bit<32>)hdr.paxos.inst, (bit<256>)hdr.paxos.paxosval);
        hdr.paxos.tmp = 16w1 << hdr.paxos.acptid;
        history2B.write((bit<32>)hdr.paxos.inst, (bit<8>)hdr.paxos.tmp);
    }
    @name(".read_round") action read_round() {
        rounds_register.read(meta.local_metadata.round, (bit<32>)hdr.paxos.inst);
        meta.local_metadata.set_drop = 1w1;
        history2B.read(meta.local_metadata.acceptors, (bit<32>)hdr.paxos.inst);
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
    @name(".learner_tbl") table learner_tbl {
        actions = {
            handle_2b;
            _drop;
        }
        key = {
            hdr.paxos.msgtype: exact;
        }
    }
    @name(".reset_tbl") table reset_tbl {
        actions = {
            handle_new_value;
            _drop;
        }
        key = {
            hdr.paxos.msgtype: exact;
        }
    }
    @name(".round_tbl") table round_tbl {
        actions = {
            read_round;
        }
        size = 1;
    }
    apply {
        if (hdr.ipv4.isValid()) {
            if (hdr.paxos.isValid()) {
                round_tbl.apply();
                if (hdr.paxos.rnd > meta.local_metadata.round) {
                    reset_tbl.apply();
                }
                else {
                    if (hdr.paxos.rnd == meta.local_metadata.round) {
                        learner_tbl.apply();
                    }
                }
                if (meta.local_metadata.acceptors == 8w6 || meta.local_metadata.acceptors == 8w5 || meta.local_metadata.acceptors == 8w3) {
                    forward_tbl.apply();
                }
            }
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

