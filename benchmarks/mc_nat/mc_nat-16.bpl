
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
var meta.intrinsic_metadata.mcast_grp:bv16;
var meta.intrinsic_metadata.lf_field_list:bv32;
var meta.intrinsic_metadata.egress_rid:bv16;
var meta.intrinsic_metadata.ingress_global_timestamp:bv32;

// Struct headers

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

// Header udp_t
var hdr.udp:Ref;
var hdr.udp.srcPort:bv16;
var hdr.udp.dstPort:bv16;
var hdr.udp.length_:bv16;
var hdr.udp.checksum:bv16;

// Table nat_table Actionlist Declaration
type nat_table.action;
const unique action.do_nat_0 : nat_table.action;
const unique action._drop_0 : nat_table.action;
axiom(forall action:nat_table.action :: action==action.do_nat_0 || action==action._drop_0);
var nat_table.action_run : nat_table.action;

// Table set_mcg Actionlist Declaration
type set_mcg.action;
const unique action.set_output_mcg_0 : set_mcg.action;
const unique action._drop_1 : set_mcg.action;
axiom(forall action:set_mcg.action :: action==action.set_output_mcg_0 || action==action._drop_1);
var set_mcg.action_run : set_mcg.action;

function {:bvbuiltin "bvadd"} add.bv8(left:bv8, right:bv8) returns(bv8);

var isValid:[Ref]bool;

var emit:[Ref]bool;
var stack.index:[HeaderStack]int;
var size:[HeaderStack]int;

type packet_in = bv336;
const packet:packet_in;

const hdr:Ref;

const meta:Ref;

const standard_metadata:Ref;

//type packet_out = packet_in;
//var packet_o:packet_in;
var drop:bool;

procedure mainProcedure()
modifies drop, meta.intrinsic_metadata.mcast_grp, hdr.ipv4.dstAddr, forward, stack.index, isValid, emit, hdr.ipv4.ttl;
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

//Parser State parse_ethernet
procedure {:inline 1} parse_ethernet()
modifies isValid;
{
	call packet_in.extract(hdr.ethernet);
	if(hdr.ethernet.etherType == 2048bv16){
		call parse_ipv4();
	}
}

//Parser State parse_ipv4
procedure {:inline 1} parse_ipv4()
modifies isValid;
{
	call packet_in.extract(hdr.ipv4);
	if(hdr.ipv4.protocol == 17bv8){
		call parse_udp();
	}
}

//Parser State parse_udp
procedure {:inline 1} parse_udp()
modifies isValid;
{
	call packet_in.extract(hdr.udp);
	call accept();
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
modifies drop, hdr.ipv4.dstAddr, hdr.ipv4.ttl;
{
	call nat_table.apply();
}

// Action NoAction_0
procedure {:inline 1} NoAction_0()
{
}

// Action do_nat_0
procedure {:inline 1} do_nat_0(dst_ip:bv32)
modifies hdr.ipv4.dstAddr, hdr.ipv4.ttl;
{
	hdr.ipv4.dstAddr := dst_ip;
	hdr.ipv4.ttl := add.bv8(hdr.ipv4.ttl, 255bv8);
}

// Action _drop_0
procedure {:inline 1} _drop_0()
modifies drop;
{
	call mark_to_drop();
}

// Table nat_table
procedure {:inline 1} nat_table.apply()
modifies drop, hdr.ipv4.dstAddr, hdr.ipv4.ttl;
{
	var dst_ip:bv32;
	if(nat_table.action_run == action.do_nat_0){
		call do_nat_0(dst_ip);
	}
	else if(nat_table.action_run == action._drop_0){
		call _drop_0();
	}
	else {
		call mark_to_drop();
	}
}

// Control ingress
procedure {:inline 1} ingress()
modifies drop, meta.intrinsic_metadata.mcast_grp;
{
	call set_mcg.apply();
}

// Action NoAction_1
procedure {:inline 1} NoAction_1()
{
}

// Action set_output_mcg_0
procedure {:inline 1} set_output_mcg_0(mcast_group:bv16)
modifies meta.intrinsic_metadata.mcast_grp;
{
	meta.intrinsic_metadata.mcast_grp := mcast_group;
}

// Action _drop_1
procedure {:inline 1} _drop_1()
modifies drop;
{
	call mark_to_drop();
}

// Table set_mcg
procedure {:inline 1} set_mcg.apply()
modifies drop, meta.intrinsic_metadata.mcast_grp;
{
	var mcast_group:bv16;
	if(set_mcg.action_run == action.set_output_mcg_0){
		call set_output_mcg_0(mcast_group);
	}
	else if(set_mcg.action_run == action._drop_1){
		call _drop_1();
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
	call packet_out.emit.headers.ipv4(hdr.ipv4);
	call packet_out.emit.headers.udp(hdr.udp);
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
modifies drop, meta.intrinsic_metadata.mcast_grp, hdr.ipv4.dstAddr, isValid, emit, hdr.ipv4.ttl;
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

procedure packet_out.emit.headers.ethernet(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.ipv4(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure packet_out.emit.headers.udp(header:Ref);
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
