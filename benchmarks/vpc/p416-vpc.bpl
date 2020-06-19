
type Ref;
type HeaderStack = [int]Ref;
var last:[HeaderStack]Ref;
var forward:bool;

// Struct standard_metadata_t
var standard_metadata.ingress_port:bv9;
var standard_metadata.egress_spec:bv9;
var standard_metadata.egress_port:bv9;
var standard_metadata.clone_spec:bv32;
var standard_metadata.instance_type:bv32;
var standard_metadata.drop:bv1;
var standard_metadata.recirculate_port:bv16;
var standard_metadata.packet_length:bv32;
var standard_metadata.enq_timestamp:bv32;
var standard_metadata.enq_qdepth:bv19;
var standard_metadata.deq_timedelta:bv32;
var standard_metadata.deq_qdepth:bv19;
var standard_metadata.ingress_global_timestamp:bv48;
var standard_metadata.egress_global_timestamp:bv48;
var standard_metadata.lf_field_list:bv32;
var standard_metadata.mcast_grp:bv16;
var standard_metadata.resubmit_flag:bv32;
var standard_metadata.egress_rid:bv16;
var standard_metadata.checksum_error:bv1;
var standard_metadata.recirculate_flag:bv32;

// Struct metadata
var meta.ingress_metadata.srcAddr:bv32;
var meta.ingress_metadata.dstAddr:bv32;
var meta.ingress_metadata.customer:bv32;

// Struct headers

// Header arp_rarp_t
var hdr.arp_rarp:Ref;
var hdr.arp_rarp.hwType:bv16;
var hdr.arp_rarp.protoType:bv16;
var hdr.arp_rarp.hwAddrLen:bv8;
var hdr.arp_rarp.protoAddrLen:bv8;
var hdr.arp_rarp.opcode:bv16;

// Header arp_rarp_ipv4_t
var hdr.arp_rarp_ipv4:Ref;
var hdr.arp_rarp_ipv4.srcHwAddr:bv48;
var hdr.arp_rarp_ipv4.srcProtoAddr:bv32;
var hdr.arp_rarp_ipv4.dstHwAddr:bv48;
var hdr.arp_rarp_ipv4.dstProtoAddr:bv32;

// Header ethernet_t
var hdr.ethernet:Ref;
var hdr.ethernet.dstAddr:bv48;
var hdr.ethernet.srcAddr:bv48;
var hdr.ethernet.etherType:bv16;

// Header ipv4_t
var hdr.ipv4:Ref;
var hdr.ipv4.version:bv4;
var hdr.ipv4.ihl:bv4;
var hdr.ipv4.diffserv:bv8;
var hdr.ipv4.totalLen:bv16;
var hdr.ipv4.identification:bv16;
var hdr.ipv4.flags:bv3;
var hdr.ipv4.fragOffset:bv13;
var hdr.ipv4.ttl:bv8;
var hdr.ipv4.protocol:bv8;
var hdr.ipv4.hdrChecksum:bv16;
var hdr.ipv4.srcAddr:bv32;
var hdr.ipv4.dstAddr:bv32;

// Header vpc_t
var hdr.vpc:Ref;
var hdr.vpc.srcSw:bv48;
var hdr.vpc.dstSw:bv48;
var hdr.vpc.customer:bv32;
var hdr.vpc.srcAddr:bv32;
var hdr.vpc.dstAddr:bv32;
var hdr.vpc.etherType:bv16;

// Table address_arp_packet Actionlist Declaration
type address_arp_packet.action;
const unique action.set_address_arp_packet_0 : address_arp_packet.action;
axiom(forall action:address_arp_packet.action :: action==action.set_address_arp_packet_0);
var address_arp_packet.action_run : address_arp_packet.action;

// Table address_ip_packet Actionlist Declaration
type address_ip_packet.action;
const unique action.set_address_ip_packet_0 : address_ip_packet.action;
axiom(forall action:address_ip_packet.action :: action==action.set_address_ip_packet_0);
var address_ip_packet.action_run : address_ip_packet.action;

// Table arp_reply Actionlist Declaration
type arp_reply.action;
const unique action._drop_0 : arp_reply.action;
const unique action.set_arp_reply_0 : arp_reply.action;
axiom(forall action:arp_reply.action :: action==action._drop_0 || action==action.set_arp_reply_0);
var arp_reply.action_run : arp_reply.action;

// Table deliver_vpc Actionlist Declaration
type deliver_vpc.action;
const unique action.pop_route_vpc_0 : deliver_vpc.action;
axiom(forall action:deliver_vpc.action :: action==action.pop_route_vpc_0);
var deliver_vpc.action_run : deliver_vpc.action;

// Table encapsulate_vpc Actionlist Declaration
type encapsulate_vpc.action;
const unique action.push_vpc_0 : encapsulate_vpc.action;
axiom(forall action:encapsulate_vpc.action :: action==action.push_vpc_0);
var encapsulate_vpc.action_run : encapsulate_vpc.action;

// Table l2_addr Actionlist Declaration
type l2_addr.action;
const unique action._noop_0 : l2_addr.action;
const unique action.set_l2_addr_0 : l2_addr.action;
axiom(forall action:l2_addr.action :: action==action._noop_0 || action==action.set_l2_addr_0);
var l2_addr.action_run : l2_addr.action;

// Table routing_vpc Actionlist Declaration
type routing_vpc.action;
const unique action.route_vpc_0 : routing_vpc.action;
axiom(forall action:routing_vpc.action :: action==action.route_vpc_0);
var routing_vpc.action_run : routing_vpc.action;

// Table vpc_customer Actionlist Declaration
type vpc_customer.action;
const unique action._drop_3 : vpc_customer.action;
const unique action.set_vpc_customer_0 : vpc_customer.action;
axiom(forall action:vpc_customer.action :: action==action._drop_3 || action==action.set_vpc_customer_0);
var vpc_customer.action_run : vpc_customer.action;

// Table vpc_dst Actionlist Declaration
type vpc_dst.action;
const unique action._drop_4 : vpc_dst.action;
const unique action.set_vpc_dst_0 : vpc_dst.action;
axiom(forall action:vpc_dst.action :: action==action._drop_4 || action==action.set_vpc_dst_0);
var vpc_dst.action_run : vpc_dst.action;

// Table vpc_sw_id Actionlist Declaration
type vpc_sw_id.action;
const unique action.set_vpc_src_sw_id_0 : vpc_sw_id.action;
axiom(forall action:vpc_sw_id.action :: action==action.set_vpc_src_sw_id_0);
var vpc_sw_id.action_run : vpc_sw_id.action;

function {:bvbuiltin "bvugt"} bugt.bv32(left:bv32, right:bv32) returns(bool);

var isValid:[Ref]bool;

var emit:[Ref]bool;
var stack.index:[HeaderStack]int;
var size:[HeaderStack]int;

type packet_in = bv704;
const packet:packet_in;

const hdr:Ref;

const meta:Ref;

const standard_metadata:Ref;

//type packet_out = packet_in;
//var packet_o:packet_in;
var drop:bool;

procedure mainProcedure()
modifies drop, hdr.vpc.srcSw, meta.ingress_metadata.dstAddr, hdr.arp_rarp_ipv4.srcProtoAddr, forward, hdr.vpc.etherType, isValid, hdr.arp_rarp.opcode, hdr.vpc.dstAddr, meta.ingress_metadata.srcAddr, hdr.ethernet.etherType, standard_metadata.egress_spec, hdr.vpc.customer, hdr.ethernet.srcAddr, hdr.vpc.srcAddr, hdr.arp_rarp_ipv4.srcHwAddr, stack.index, hdr.ethernet.dstAddr, meta.ingress_metadata.customer, hdr.vpc.dstSw, emit, hdr.arp_rarp_ipv4.dstProtoAddr, hdr.arp_rarp_ipv4.dstHwAddr;
{
	call clear_drop();
	call init.stack.index();
	call clear_emit();
	call clear_valid();
	call clear_forward();
	call main();
}

procedure clear_forward();
	ensures forward==false;
	modifies forward;

// Parser ParserImpl
procedure {:inline 1} ParserImpl()
modifies isValid;
{
	call start();
}

//Parser State parse_arp_rarp
procedure {:inline 1} parse_arp_rarp()
modifies isValid;
{
	call packet_in.extract(hdr.arp_rarp);
	if(hdr.arp_rarp.protoType == 2048bv16){
		call parse_arp_rarp_ipv4();
	}
}

//Parser State parse_arp_rarp_ipv4
procedure {:inline 1} parse_arp_rarp_ipv4()
modifies isValid;
{
	call packet_in.extract(hdr.arp_rarp_ipv4);
	call accept();
}

//Parser State parse_ethernet
procedure {:inline 1} parse_ethernet()
modifies isValid;
{
	call packet_in.extract(hdr.ethernet);
	if(hdr.ethernet.etherType == 1911bv16){
		call parse_vpc();
	}
	else if(hdr.ethernet.etherType == 2048bv16){
		call parse_ipv4();
	}
	else if(hdr.ethernet.etherType == 2054bv16){
		call parse_arp_rarp();
	}
}

//Parser State parse_ipv4
procedure {:inline 1} parse_ipv4()
modifies isValid;
{
	call packet_in.extract(hdr.ipv4);
	call accept();
}

//Parser State parse_vpc
procedure {:inline 1} parse_vpc()
modifies isValid;
{
	call packet_in.extract(hdr.vpc);
	if(hdr.vpc.etherType == 2048bv16){
		call parse_ipv4();
	}
	else if(hdr.vpc.etherType == 2054bv16){
		call parse_arp_rarp();
	}
}

//Parser State start
procedure {:inline 1} start()
modifies isValid;
{
	call parse_ethernet();
}

//Parser State accept
procedure {:inline 1} accept()
{
}

//Parser State reject
procedure {:inline 1} reject()
{
}

// Control egress
procedure {:inline 1} egress()
{
}

// Control ingress
procedure {:inline 1} ingress()
modifies drop, hdr.vpc.srcSw, meta.ingress_metadata.dstAddr, hdr.arp_rarp_ipv4.srcProtoAddr, forward, hdr.vpc.etherType, isValid, hdr.arp_rarp.opcode, hdr.vpc.dstAddr, meta.ingress_metadata.srcAddr, hdr.ethernet.etherType, standard_metadata.egress_spec, hdr.vpc.customer, hdr.ethernet.srcAddr, hdr.vpc.srcAddr, hdr.arp_rarp_ipv4.srcHwAddr, hdr.ethernet.dstAddr, meta.ingress_metadata.customer, hdr.vpc.dstSw, hdr.arp_rarp_ipv4.dstProtoAddr, hdr.arp_rarp_ipv4.dstHwAddr;
{
	if((hdr.ethernet.etherType==2054bv16) && (hdr.arp_rarp.opcode==1bv16)){
		call address_arp_packet.apply();
	}
	else {
		if(hdr.ethernet.etherType==2048bv16){
			call address_ip_packet.apply();
		}
	}
	if(((hdr.ethernet.etherType==2054bv16) && (hdr.arp_rarp.opcode==1bv16))||(hdr.ethernet.etherType==2048bv16)){
		call vpc_customer.apply();
	}
	if((hdr.ethernet.etherType==2054bv16) && (hdr.arp_rarp.opcode==1bv16)){
		if(bugt.bv32(meta.ingress_metadata.customer, 0bv32)){
			call arp_reply.apply();
		}
	}
	else {
		if(hdr.ethernet.etherType==2048bv16){
			if(bugt.bv32(meta.ingress_metadata.customer, 0bv32)){
				call encapsulate_vpc.apply();
				call vpc_sw_id.apply();
				call vpc_dst.apply();
			}
		}
	}
	if(isValid[hdr.vpc]){
		call l2_addr.apply();
		call routing_vpc.apply();
		call deliver_vpc.apply();
	}
}

// Action NoAction_0
procedure {:inline 1} NoAction_0()
{
}

// Action NoAction_11
procedure {:inline 1} NoAction_11()
{
}

// Action NoAction_12
procedure {:inline 1} NoAction_12()
{
}

// Action NoAction_13
procedure {:inline 1} NoAction_13()
{
}

// Action NoAction_14
procedure {:inline 1} NoAction_14()
{
}

// Action NoAction_15
procedure {:inline 1} NoAction_15()
{
}

// Action NoAction_16
procedure {:inline 1} NoAction_16()
{
}

// Action NoAction_17
procedure {:inline 1} NoAction_17()
{
}

// Action NoAction_18
procedure {:inline 1} NoAction_18()
{
}

// Action NoAction_19
procedure {:inline 1} NoAction_19()
{
}

// Action set_address_arp_packet_0
procedure {:inline 1} set_address_arp_packet_0()
modifies meta.ingress_metadata.dstAddr, meta.ingress_metadata.customer, meta.ingress_metadata.srcAddr;
{
	meta.ingress_metadata.customer := 0bv32;
	meta.ingress_metadata.srcAddr := hdr.arp_rarp_ipv4.srcProtoAddr;
	meta.ingress_metadata.dstAddr := hdr.arp_rarp_ipv4.dstProtoAddr;
}

// Action set_address_ip_packet_0
procedure {:inline 1} set_address_ip_packet_0()
modifies meta.ingress_metadata.dstAddr, meta.ingress_metadata.customer, meta.ingress_metadata.srcAddr;
{
	meta.ingress_metadata.customer := 0bv32;
	meta.ingress_metadata.srcAddr := hdr.ipv4.srcAddr;
	meta.ingress_metadata.dstAddr := hdr.ipv4.dstAddr;
}

// Action _drop_0
procedure {:inline 1} _drop_0()
modifies drop;
{
	call mark_to_drop();
}

// Action _drop_3
procedure {:inline 1} _drop_3()
modifies drop;
{
	call mark_to_drop();
}

// Action _drop_4
procedure {:inline 1} _drop_4()
modifies drop;
{
	call mark_to_drop();
}

// Action set_arp_reply_0
procedure {:inline 1} set_arp_reply_0(hwAddr:bv48)
modifies standard_metadata.egress_spec, hdr.ethernet.srcAddr, hdr.arp_rarp_ipv4.srcHwAddr, hdr.arp_rarp_ipv4.srcProtoAddr, forward, hdr.arp_rarp.opcode, hdr.ethernet.dstAddr, hdr.arp_rarp_ipv4.dstProtoAddr, hdr.arp_rarp_ipv4.dstHwAddr;
{
	hdr.ethernet.dstAddr := hdr.ethernet.srcAddr;
	hdr.ethernet.srcAddr := hwAddr;
	hdr.arp_rarp.opcode := 2bv16;
	hdr.arp_rarp_ipv4.dstHwAddr := hdr.arp_rarp_ipv4.srcHwAddr;
	hdr.arp_rarp_ipv4.dstProtoAddr := hdr.arp_rarp_ipv4.srcProtoAddr;
	hdr.arp_rarp_ipv4.srcHwAddr := hwAddr;
	hdr.arp_rarp_ipv4.srcProtoAddr := meta.ingress_metadata.dstAddr;
	forward := true;
	standard_metadata.egress_spec := standard_metadata.ingress_port;
}

// Action pop_route_vpc_0
procedure {:inline 1} pop_route_vpc_0(port:bv9)
modifies standard_metadata.egress_spec, forward, isValid, hdr.ethernet.etherType;
{
	forward := true;
	standard_metadata.egress_spec := port;
	hdr.ethernet.etherType := hdr.vpc.etherType;
	isValid[hdr.vpc] := false;
}

// Action push_vpc_0
procedure {:inline 1} push_vpc_0()
modifies hdr.vpc.customer, hdr.vpc.srcAddr, hdr.vpc.etherType, isValid, hdr.vpc.dstAddr, hdr.ethernet.etherType;
{
	isValid[hdr.vpc] := true;
	hdr.vpc.etherType := hdr.ethernet.etherType;
	hdr.ethernet.etherType := 1911bv16;
	hdr.vpc.customer := meta.ingress_metadata.customer;
	hdr.vpc.srcAddr := meta.ingress_metadata.srcAddr;
	hdr.vpc.dstAddr := meta.ingress_metadata.dstAddr;
}

// Action _noop_0
procedure {:inline 1} _noop_0()
{
}

// Action set_l2_addr_0
procedure {:inline 1} set_l2_addr_0(srcAddr:bv48, dstAddr:bv48)
modifies hdr.ethernet.srcAddr, hdr.ethernet.dstAddr;
{
	hdr.ethernet.srcAddr := srcAddr;
	hdr.ethernet.dstAddr := dstAddr;
}

// Action route_vpc_0
procedure {:inline 1} route_vpc_0(port:bv9)
modifies standard_metadata.egress_spec, forward;
{
	forward := true;
	standard_metadata.egress_spec := port;
}

// Action set_vpc_customer_0
procedure {:inline 1} set_vpc_customer_0(customer:bv32)
modifies meta.ingress_metadata.customer;
{
	meta.ingress_metadata.customer := customer;
}

// Action set_vpc_dst_0
procedure {:inline 1} set_vpc_dst_0(dstSw:bv48)
modifies hdr.vpc.dstSw;
{
	hdr.vpc.dstSw := dstSw;
}

// Action set_vpc_src_sw_id_0
procedure {:inline 1} set_vpc_src_sw_id_0(srcSw:bv48)
modifies hdr.vpc.srcSw;
{
	hdr.vpc.srcSw := srcSw;
}

// Table address_arp_packet
procedure {:inline 1} address_arp_packet.apply()
modifies drop, meta.ingress_metadata.dstAddr, meta.ingress_metadata.customer, meta.ingress_metadata.srcAddr;
{
	if(address_arp_packet.action_run == action.set_address_arp_packet_0){
		call set_address_arp_packet_0();
	}
	else {
		call mark_to_drop();
	}
}

// Table address_ip_packet
procedure {:inline 1} address_ip_packet.apply()
modifies drop, meta.ingress_metadata.dstAddr, meta.ingress_metadata.customer, meta.ingress_metadata.srcAddr;
{
	if(address_ip_packet.action_run == action.set_address_ip_packet_0){
		call set_address_ip_packet_0();
	}
	else {
		call mark_to_drop();
	}
}

// Table arp_reply
procedure {:inline 1} arp_reply.apply()
modifies drop, standard_metadata.egress_spec, hdr.ethernet.srcAddr, hdr.arp_rarp_ipv4.srcHwAddr, hdr.arp_rarp_ipv4.srcProtoAddr, forward, hdr.arp_rarp.opcode, hdr.ethernet.dstAddr, hdr.arp_rarp_ipv4.dstProtoAddr, hdr.arp_rarp_ipv4.dstHwAddr;
{
	var hwAddr:bv48;
	if(arp_reply.action_run == action._drop_0){
		call _drop_0();
	}
	else if(arp_reply.action_run == action.set_arp_reply_0){
		call set_arp_reply_0(hwAddr);
	}
	else {
		call mark_to_drop();
	}
}

// Table deliver_vpc
procedure {:inline 1} deliver_vpc.apply()
modifies drop, standard_metadata.egress_spec, forward, isValid, hdr.ethernet.etherType;
{
	var port:bv9;
	if(deliver_vpc.action_run == action.pop_route_vpc_0){
		call pop_route_vpc_0(port);
	}
	else {
		call mark_to_drop();
	}
}

// Table encapsulate_vpc
procedure {:inline 1} encapsulate_vpc.apply()
modifies drop, hdr.vpc.customer, hdr.vpc.srcAddr, hdr.vpc.etherType, isValid, hdr.vpc.dstAddr, hdr.ethernet.etherType;
{
	if(encapsulate_vpc.action_run == action.push_vpc_0){
		call push_vpc_0();
	}
	else {
		call mark_to_drop();
	}
}

// Table l2_addr
procedure {:inline 1} l2_addr.apply()
modifies drop, hdr.ethernet.srcAddr, hdr.ethernet.dstAddr;
{
	var srcAddr:bv48;
	var dstAddr:bv48;
	if(l2_addr.action_run == action._noop_0){
		call _noop_0();
	}
	else if(l2_addr.action_run == action.set_l2_addr_0){
		call set_l2_addr_0(srcAddr, dstAddr);
	}
	else {
		call mark_to_drop();
	}
}

// Table routing_vpc
procedure {:inline 1} routing_vpc.apply()
modifies drop, standard_metadata.egress_spec, forward;
{
	var port:bv9;
	if(routing_vpc.action_run == action.route_vpc_0){
		call route_vpc_0(port);
	}
	else {
		call mark_to_drop();
	}
}

// Table vpc_customer
procedure {:inline 1} vpc_customer.apply()
modifies drop, meta.ingress_metadata.customer;
{
	var customer:bv32;
	if(vpc_customer.action_run == action._drop_3){
		call _drop_3();
	}
	else if(vpc_customer.action_run == action.set_vpc_customer_0){
		call set_vpc_customer_0(customer);
	}
	else {
		call mark_to_drop();
	}
}

// Table vpc_dst
procedure {:inline 1} vpc_dst.apply()
modifies drop, hdr.vpc.dstSw;
{
	var dstSw:bv48;
	if(vpc_dst.action_run == action._drop_4){
		call _drop_4();
	}
	else if(vpc_dst.action_run == action.set_vpc_dst_0){
		call set_vpc_dst_0(dstSw);
	}
	else {
		call mark_to_drop();
	}
}

// Table vpc_sw_id
procedure {:inline 1} vpc_sw_id.apply()
modifies drop, hdr.vpc.srcSw;
{
	var srcSw:bv48;
	if(vpc_sw_id.action_run == action.set_vpc_src_sw_id_0){
		call set_vpc_src_sw_id_0(srcSw);
	}
	else {
		call mark_to_drop();
	}
}

// Control DeparserImpl
procedure {:inline 1} DeparserImpl()
modifies emit;
{
	call packet_out.emit.headers.ethernet(hdr.ethernet);
	call packet_out.emit.headers.vpc(hdr.vpc);
	call packet_out.emit.headers.arp_rarp(hdr.arp_rarp);
	call packet_out.emit.headers.arp_rarp_ipv4(hdr.arp_rarp_ipv4);
	call packet_out.emit.headers.ipv4(hdr.ipv4);
}

// Control verifyChecksum
procedure {:inline 1} verifyChecksum()
{
}

// Control computeChecksum
procedure {:inline 1} computeChecksum()
{
}
procedure {:inline 1} main()
modifies drop, hdr.vpc.srcSw, meta.ingress_metadata.dstAddr, hdr.arp_rarp_ipv4.srcProtoAddr, forward, hdr.vpc.etherType, isValid, hdr.arp_rarp.opcode, hdr.vpc.dstAddr, meta.ingress_metadata.srcAddr, hdr.ethernet.etherType, standard_metadata.egress_spec, hdr.vpc.customer, hdr.ethernet.srcAddr, hdr.vpc.srcAddr, hdr.arp_rarp_ipv4.srcHwAddr, hdr.ethernet.dstAddr, meta.ingress_metadata.customer, hdr.vpc.dstSw, emit, hdr.arp_rarp_ipv4.dstProtoAddr, hdr.arp_rarp_ipv4.dstHwAddr;
{
	call ParserImpl();
	if(drop != true){
		call verifyChecksum();
		call ingress();
		call egress();
		call computeChecksum();
		call DeparserImpl();
	}
}

procedure clear_valid();
	ensures (forall header:Ref:: isValid[header]==false);
	modifies isValid;

procedure clear_emit();
	ensures (forall header:Ref:: emit[header]==false);
	modifies emit;

procedure init.stack.index();
ensures (forall s:HeaderStack::stack.index[s]==0);
	modifies stack.index;

procedure packet_in.extract(header:Ref);
	ensures (isValid[header] == true);
	modifies isValid;

procedure packet_out.emit.headers.arp_rarp(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.arp_rarp_ipv4(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.ethernet(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.ipv4(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.vpc(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure {:inline 1} mark_to_drop()
modifies drop;
{
	drop := true;
}

procedure clear_drop();
	ensures forward==false;
	modifies drop;
