#include <core.p4>
#include <v1model.p4>

header eth_hdr {
    bit<48> dst;
    bit<48> src;
    bit<16> etype;
}

struct metadata {
}

struct headers {
    @name(".eth") 
    eth_hdr eth;
}

parser ParserImpl(packet_in packet, out headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".eth_parse") state eth_parse {
        packet.extract(hdr.eth);
        transition accept;
    }
    @name(".start") state start {
        transition eth_parse;
    }
}

control ingress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".fwd_act") action fwd_act(bit<9> prt) {
        standard_metadata.egress_spec = prt;
    }
    @name(".drop_act") action drop_act() {
        mark_to_drop();
    }
    @name(".in_tbl") table in_tbl {
        actions = {
            fwd_act;
            drop_act;
        }
        key = {
            standard_metadata.ingress_port: exact;
        }
    }
    apply {
        in_tbl.apply();
    }
}

control egress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    apply {
    }
}

control DeparserImpl(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.eth);
    }
}

control verifyChecksum(inout headers hdr, inout metadata meta) {
    apply {
    }
}

control computeChecksum(inout headers hdr, inout metadata meta) {
    apply {
    }
}

V1Switch(ParserImpl(), verifyChecksum(), ingress(), egress(), computeChecksum(), DeparserImpl()) main;

