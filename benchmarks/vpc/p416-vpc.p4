#include <core.p4>
#include <v1model.p4>

struct ingress_metadata_t {
    bit<32> srcAddr;
    bit<32> dstAddr;
    bit<32> customer;
}

header arp_rarp_t {
    bit<16> hwType;
    bit<16> protoType;
    bit<8>  hwAddrLen;
    bit<8>  protoAddrLen;
    bit<16> opcode;
}

header arp_rarp_ipv4_t {
    bit<48> srcHwAddr;
    bit<32> srcProtoAddr;
    bit<48> dstHwAddr;
    bit<32> dstProtoAddr;
}

header ethernet_t {
    bit<48> dstAddr;
    bit<48> srcAddr;
    bit<16> etherType;
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

header vpc_t {
    bit<48> srcSw;
    bit<48> dstSw;
    bit<32> customer;
    bit<32> srcAddr;
    bit<32> dstAddr;
    bit<16> etherType;
}

struct metadata {
    @name(".ingress_metadata") 
    ingress_metadata_t ingress_metadata;
}

struct headers {
    @name(".arp_rarp") 
    arp_rarp_t      arp_rarp;
    @name(".arp_rarp_ipv4") 
    arp_rarp_ipv4_t arp_rarp_ipv4;
    @name(".ethernet") 
    ethernet_t      ethernet;
    @name(".ipv4") 
    ipv4_t          ipv4;
    @name(".vpc") 
    vpc_t           vpc;
}

parser ParserImpl(packet_in packet, out headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".parse_arp_rarp") state parse_arp_rarp {
        packet.extract(hdr.arp_rarp);
        transition select(hdr.arp_rarp.protoType) {
            16w0x800: parse_arp_rarp_ipv4;
            default: accept;
        }
    }
    @name(".parse_arp_rarp_ipv4") state parse_arp_rarp_ipv4 {
        packet.extract(hdr.arp_rarp_ipv4);
        transition accept;
    }
    @name(".parse_ethernet") state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            16w0x777: parse_vpc;
            16w0x800: parse_ipv4;
            16w0x806: parse_arp_rarp;
            default: accept;
        }
    }
    @name(".parse_ipv4") state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition accept;
    }
    @name(".parse_vpc") state parse_vpc {
        packet.extract(hdr.vpc);
        transition select(hdr.vpc.etherType) {
            16w0x800: parse_ipv4;
            16w0x806: parse_arp_rarp;
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

control ingress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    @name(".set_address_arp_packet") action set_address_arp_packet() {
        meta.ingress_metadata.customer = 32w0;
        meta.ingress_metadata.srcAddr = hdr.arp_rarp_ipv4.srcProtoAddr;
        meta.ingress_metadata.dstAddr = hdr.arp_rarp_ipv4.dstProtoAddr;
    }
    @name(".set_address_ip_packet") action set_address_ip_packet() {
        meta.ingress_metadata.customer = 32w0;
        meta.ingress_metadata.srcAddr = hdr.ipv4.srcAddr;
        meta.ingress_metadata.dstAddr = hdr.ipv4.dstAddr;
    }
    @name("._drop") action _drop() {
        mark_to_drop();
    }
    @name(".set_arp_reply") action set_arp_reply(bit<48> hwAddr) {
        hdr.ethernet.dstAddr = hdr.ethernet.srcAddr;
        hdr.ethernet.srcAddr = hwAddr;
        hdr.arp_rarp.opcode = 16w2;
        hdr.arp_rarp_ipv4.dstHwAddr = hdr.arp_rarp_ipv4.srcHwAddr;
        hdr.arp_rarp_ipv4.dstProtoAddr = hdr.arp_rarp_ipv4.srcProtoAddr;
        hdr.arp_rarp_ipv4.srcHwAddr = hwAddr;
        hdr.arp_rarp_ipv4.srcProtoAddr = meta.ingress_metadata.dstAddr;
        standard_metadata.egress_spec = standard_metadata.ingress_port;
    }
    @name(".pop_route_vpc") action pop_route_vpc(bit<9> port) {
        standard_metadata.egress_spec = port;
        hdr.ethernet.etherType = hdr.vpc.etherType;
        hdr.vpc.setInvalid();
    }
    @name(".push_vpc") action push_vpc() {
        hdr.vpc.setValid();
        hdr.vpc.etherType = hdr.ethernet.etherType;
        hdr.ethernet.etherType = 16w0x777;
        hdr.vpc.customer = meta.ingress_metadata.customer;
        hdr.vpc.srcAddr = meta.ingress_metadata.srcAddr;
        hdr.vpc.dstAddr = meta.ingress_metadata.dstAddr;
    }
    @name("._noop") action _noop() {
        ;
    }
    @name(".set_l2_addr") action set_l2_addr(bit<48> srcAddr, bit<48> dstAddr) {
        hdr.ethernet.srcAddr = srcAddr;
        hdr.ethernet.dstAddr = dstAddr;
    }
    @name(".route_vpc") action route_vpc(bit<9> port) {
        standard_metadata.egress_spec = port;
    }
    @name(".set_vpc_customer") action set_vpc_customer(bit<32> customer) {
        meta.ingress_metadata.customer = customer;
    }
    @name(".set_vpc_dst") action set_vpc_dst(bit<48> dstSw) {
        hdr.vpc.dstSw = dstSw;
    }
    @name(".set_vpc_src_sw_id") action set_vpc_src_sw_id(bit<48> srcSw) {
        hdr.vpc.srcSw = srcSw;
    }
    @name(".address_arp_packet") table address_arp_packet {
        actions = {
            set_address_arp_packet;
        }
        size = 1;
    }
    @name(".address_ip_packet") table address_ip_packet {
        actions = {
            set_address_ip_packet;
        }
        size = 1;
    }
    @name(".arp_reply") table arp_reply {
        actions = {
            _drop;
            set_arp_reply;
        }
        key = {
            meta.ingress_metadata.customer: exact;
            meta.ingress_metadata.srcAddr : lpm;
            meta.ingress_metadata.dstAddr : exact;
        }
        size = 1024;
    }
    @name(".deliver_vpc") table deliver_vpc {
        actions = {
            pop_route_vpc;
        }
        key = {
            hdr.vpc.dstSw   : exact;
            hdr.vpc.customer: exact;
            hdr.vpc.dstAddr : lpm;
        }
        size = 1024;
    }
    @name(".encapsulate_vpc") table encapsulate_vpc {
        actions = {
            push_vpc;
        }
        size = 1;
    }
    @name(".l2_addr") table l2_addr {
        actions = {
            _noop;
            set_l2_addr;
        }
        key = {
            hdr.vpc.customer: exact;
            hdr.vpc.dstSw   : exact;
            hdr.vpc.srcAddr : lpm;
            hdr.vpc.dstAddr : exact;
        }
        size = 1024;
    }
    @name(".routing_vpc") table routing_vpc {
        actions = {
            route_vpc;
        }
        key = {
            hdr.vpc.dstSw: exact;
        }
        size = 1024;
    }
    @name(".vpc_customer") table vpc_customer {
        actions = {
            _drop;
            set_vpc_customer;
        }
        key = {
            hdr.ethernet.srcAddr         : exact;
            meta.ingress_metadata.srcAddr: exact;
        }
        size = 1024;
    }
    @name(".vpc_dst") table vpc_dst {
        actions = {
            _drop;
            set_vpc_dst;
        }
        key = {
            hdr.vpc.customer: exact;
            hdr.vpc.dstAddr : lpm;
        }
        size = 1024;
    }
    @name(".vpc_sw_id") table vpc_sw_id {
        actions = {
            set_vpc_src_sw_id;
        }
        size = 1;
    }
    apply {
        if (hdr.ethernet.etherType == 16w0x806 && hdr.arp_rarp.opcode == 16w1) {
            address_arp_packet.apply();
        }
        else {
            if (hdr.ethernet.etherType == 16w0x800) {
                address_ip_packet.apply();
            }
        }
        if (hdr.ethernet.etherType == 16w0x806 && hdr.arp_rarp.opcode == 16w1 || hdr.ethernet.etherType == 16w0x800) {
            vpc_customer.apply();
        }
        if (hdr.ethernet.etherType == 16w0x806 && hdr.arp_rarp.opcode == 16w1) {
            if (meta.ingress_metadata.customer > 32w0) {
                arp_reply.apply();
            }
        }
        else {
            if (hdr.ethernet.etherType == 16w0x800) {
                if (meta.ingress_metadata.customer > 32w0) {
                    encapsulate_vpc.apply();
                    vpc_sw_id.apply();
                    vpc_dst.apply();
                }
            }
        }
        if (hdr.vpc.isValid()) {
            l2_addr.apply();
            routing_vpc.apply();
            deliver_vpc.apply();
        }
    }
}

control DeparserImpl(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.vpc);
        packet.emit(hdr.arp_rarp);
        packet.emit(hdr.arp_rarp_ipv4);
        packet.emit(hdr.ipv4);
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

