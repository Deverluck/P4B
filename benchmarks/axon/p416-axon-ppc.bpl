
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
var meta.my_metadata.fwdHopCount:bv8;
var meta.my_metadata.revHopCount:bv8;
var meta.my_metadata.headerLen:bv16;

// Struct headers

// Header axon_head_t
var hdr.axon_head:Ref;
var hdr.axon_head.preamble:bv64;
var hdr.axon_head.axonType:bv8;
var hdr.axon_head.axonLength:bv16;
var hdr.axon_head.fwdHopCount:bv8;
var hdr.axon_head.revHopCount:bv8;
const hdr.axon_fwdHop:HeaderStack;

// Header axon_hop_t
var hdr.axon_fwdHop.last:Ref;
var hdr.axon_fwdHop.last.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.0:Ref;
var hdr.axon_fwdHop.0.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.1:Ref;
var hdr.axon_fwdHop.1.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.2:Ref;
var hdr.axon_fwdHop.2.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.3:Ref;
var hdr.axon_fwdHop.3.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.4:Ref;
var hdr.axon_fwdHop.4.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.5:Ref;
var hdr.axon_fwdHop.5.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.6:Ref;
var hdr.axon_fwdHop.6.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.7:Ref;
var hdr.axon_fwdHop.7.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.8:Ref;
var hdr.axon_fwdHop.8.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.9:Ref;
var hdr.axon_fwdHop.9.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.10:Ref;
var hdr.axon_fwdHop.10.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.11:Ref;
var hdr.axon_fwdHop.11.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.12:Ref;
var hdr.axon_fwdHop.12.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.13:Ref;
var hdr.axon_fwdHop.13.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.14:Ref;
var hdr.axon_fwdHop.14.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.15:Ref;
var hdr.axon_fwdHop.15.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.16:Ref;
var hdr.axon_fwdHop.16.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.17:Ref;
var hdr.axon_fwdHop.17.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.18:Ref;
var hdr.axon_fwdHop.18.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.19:Ref;
var hdr.axon_fwdHop.19.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.20:Ref;
var hdr.axon_fwdHop.20.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.21:Ref;
var hdr.axon_fwdHop.21.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.22:Ref;
var hdr.axon_fwdHop.22.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.23:Ref;
var hdr.axon_fwdHop.23.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.24:Ref;
var hdr.axon_fwdHop.24.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.25:Ref;
var hdr.axon_fwdHop.25.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.26:Ref;
var hdr.axon_fwdHop.26.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.27:Ref;
var hdr.axon_fwdHop.27.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.28:Ref;
var hdr.axon_fwdHop.28.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.29:Ref;
var hdr.axon_fwdHop.29.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.30:Ref;
var hdr.axon_fwdHop.30.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.31:Ref;
var hdr.axon_fwdHop.31.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.32:Ref;
var hdr.axon_fwdHop.32.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.33:Ref;
var hdr.axon_fwdHop.33.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.34:Ref;
var hdr.axon_fwdHop.34.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.35:Ref;
var hdr.axon_fwdHop.35.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.36:Ref;
var hdr.axon_fwdHop.36.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.37:Ref;
var hdr.axon_fwdHop.37.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.38:Ref;
var hdr.axon_fwdHop.38.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.39:Ref;
var hdr.axon_fwdHop.39.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.40:Ref;
var hdr.axon_fwdHop.40.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.41:Ref;
var hdr.axon_fwdHop.41.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.42:Ref;
var hdr.axon_fwdHop.42.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.43:Ref;
var hdr.axon_fwdHop.43.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.44:Ref;
var hdr.axon_fwdHop.44.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.45:Ref;
var hdr.axon_fwdHop.45.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.46:Ref;
var hdr.axon_fwdHop.46.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.47:Ref;
var hdr.axon_fwdHop.47.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.48:Ref;
var hdr.axon_fwdHop.48.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.49:Ref;
var hdr.axon_fwdHop.49.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.50:Ref;
var hdr.axon_fwdHop.50.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.51:Ref;
var hdr.axon_fwdHop.51.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.52:Ref;
var hdr.axon_fwdHop.52.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.53:Ref;
var hdr.axon_fwdHop.53.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.54:Ref;
var hdr.axon_fwdHop.54.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.55:Ref;
var hdr.axon_fwdHop.55.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.56:Ref;
var hdr.axon_fwdHop.56.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.57:Ref;
var hdr.axon_fwdHop.57.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.58:Ref;
var hdr.axon_fwdHop.58.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.59:Ref;
var hdr.axon_fwdHop.59.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.60:Ref;
var hdr.axon_fwdHop.60.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.61:Ref;
var hdr.axon_fwdHop.61.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.62:Ref;
var hdr.axon_fwdHop.62.port:bv8;

// Header axon_hop_t
var hdr.axon_fwdHop.63:Ref;
var hdr.axon_fwdHop.63.port:bv8;
const hdr.axon_revHop:HeaderStack;

// Header axon_hop_t
var hdr.axon_revHop.last:Ref;
var hdr.axon_revHop.last.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.0:Ref;
var hdr.axon_revHop.0.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.1:Ref;
var hdr.axon_revHop.1.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.2:Ref;
var hdr.axon_revHop.2.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.3:Ref;
var hdr.axon_revHop.3.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.4:Ref;
var hdr.axon_revHop.4.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.5:Ref;
var hdr.axon_revHop.5.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.6:Ref;
var hdr.axon_revHop.6.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.7:Ref;
var hdr.axon_revHop.7.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.8:Ref;
var hdr.axon_revHop.8.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.9:Ref;
var hdr.axon_revHop.9.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.10:Ref;
var hdr.axon_revHop.10.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.11:Ref;
var hdr.axon_revHop.11.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.12:Ref;
var hdr.axon_revHop.12.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.13:Ref;
var hdr.axon_revHop.13.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.14:Ref;
var hdr.axon_revHop.14.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.15:Ref;
var hdr.axon_revHop.15.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.16:Ref;
var hdr.axon_revHop.16.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.17:Ref;
var hdr.axon_revHop.17.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.18:Ref;
var hdr.axon_revHop.18.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.19:Ref;
var hdr.axon_revHop.19.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.20:Ref;
var hdr.axon_revHop.20.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.21:Ref;
var hdr.axon_revHop.21.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.22:Ref;
var hdr.axon_revHop.22.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.23:Ref;
var hdr.axon_revHop.23.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.24:Ref;
var hdr.axon_revHop.24.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.25:Ref;
var hdr.axon_revHop.25.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.26:Ref;
var hdr.axon_revHop.26.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.27:Ref;
var hdr.axon_revHop.27.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.28:Ref;
var hdr.axon_revHop.28.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.29:Ref;
var hdr.axon_revHop.29.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.30:Ref;
var hdr.axon_revHop.30.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.31:Ref;
var hdr.axon_revHop.31.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.32:Ref;
var hdr.axon_revHop.32.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.33:Ref;
var hdr.axon_revHop.33.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.34:Ref;
var hdr.axon_revHop.34.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.35:Ref;
var hdr.axon_revHop.35.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.36:Ref;
var hdr.axon_revHop.36.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.37:Ref;
var hdr.axon_revHop.37.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.38:Ref;
var hdr.axon_revHop.38.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.39:Ref;
var hdr.axon_revHop.39.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.40:Ref;
var hdr.axon_revHop.40.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.41:Ref;
var hdr.axon_revHop.41.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.42:Ref;
var hdr.axon_revHop.42.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.43:Ref;
var hdr.axon_revHop.43.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.44:Ref;
var hdr.axon_revHop.44.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.45:Ref;
var hdr.axon_revHop.45.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.46:Ref;
var hdr.axon_revHop.46.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.47:Ref;
var hdr.axon_revHop.47.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.48:Ref;
var hdr.axon_revHop.48.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.49:Ref;
var hdr.axon_revHop.49.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.50:Ref;
var hdr.axon_revHop.50.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.51:Ref;
var hdr.axon_revHop.51.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.52:Ref;
var hdr.axon_revHop.52.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.53:Ref;
var hdr.axon_revHop.53.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.54:Ref;
var hdr.axon_revHop.54.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.55:Ref;
var hdr.axon_revHop.55.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.56:Ref;
var hdr.axon_revHop.56.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.57:Ref;
var hdr.axon_revHop.57.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.58:Ref;
var hdr.axon_revHop.58.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.59:Ref;
var hdr.axon_revHop.59.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.60:Ref;
var hdr.axon_revHop.60.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.61:Ref;
var hdr.axon_revHop.61.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.62:Ref;
var hdr.axon_revHop.62.port:bv8;

// Header axon_hop_t
var hdr.axon_revHop.63:Ref;
var hdr.axon_revHop.63.port:bv8;
var parser.tmp_0:bv64;

// Table drop_pkt Actionlist Declaration
type drop_pkt.action;
const unique action._drop_0 : drop_pkt.action;
axiom(forall action:drop_pkt.action :: action==action._drop_0);
var drop_pkt.action_run : drop_pkt.action;

// Table route_pkt Actionlist Declaration
type route_pkt.action;
const unique action._drop_2 : route_pkt.action;
const unique action.route_0 : route_pkt.action;
axiom(forall action:route_pkt.action :: action==action._drop_2 || action==action.route_0);
var route_pkt.action_run : route_pkt.action;

function {:bvbuiltin "bvadd"} add.bv8(left:bv8, right:bv8) returns(bv8);

var isValid:[Ref]bool;

var emit:[Ref]bool;
var stack.index:[HeaderStack]int;
var size:[HeaderStack]int;

type packet_in = bv1128;
const packet:packet_in;

const hdr:Ref;

const meta:Ref;

const standard_metadata:Ref;

//type packet_out = packet_in;
//var packet_o:packet_in;
var drop:bool;

procedure mainProcedure()
modifies drop, hdr.axon_revHop.0.port, forward, isValid, meta.my_metadata.headerLen, hdr.axon_head.revHopCount, standard_metadata.egress_spec, meta.my_metadata.fwdHopCount, stack.index, meta.my_metadata.revHopCount, emit, parser.tmp_0, hdr.axon_head.fwdHopCount;
{
	call clear_drop();
	call init.stack.index();
	call clear_emit();
	call clear_valid();
	call clear_forward();
	call main();

	// Check Implicit Drop
	assert(forward||drop);
}

procedure clear_forward();
	ensures forward==false;
	modifies forward;

// Parser ParserImpl
procedure {:inline 1} ParserImpl()
modifies meta.my_metadata.fwdHopCount, isValid, meta.my_metadata.revHopCount, meta.my_metadata.headerLen, parser.tmp_0;
{
	call start();
}

//Parser State parse_fwdHop
procedure {:inline 1} parse_fwdHop()
modifies meta.my_metadata.fwdHopCount, stack.index, isValid;
{
	call packet_in.extract.headers.axon_fwdHop.next(hdr.axon_fwdHop);
	meta.my_metadata.fwdHopCount := add.bv8(meta.my_metadata.fwdHopCount, 255bv8);
	call parse_next_fwdHop();
}

//Parser State parse_head
procedure {:inline 1} parse_head()
modifies meta.my_metadata.fwdHopCount, isValid, meta.my_metadata.revHopCount, meta.my_metadata.headerLen;
{
	call packet_in.extract(hdr.axon_head);
	meta.my_metadata.fwdHopCount := hdr.axon_head.fwdHopCount;
	meta.my_metadata.revHopCount := hdr.axon_head.revHopCount;
	meta.my_metadata.headerLen := 0bv8++add.bv8(add.bv8(2bv8, hdr.axon_head.fwdHopCount), hdr.axon_head.revHopCount);
	if(hdr.axon_head.fwdHopCount == 0bv8){
		call accept();
	}
}

//Parser State parse_next_fwdHop
procedure {:inline 1} parse_next_fwdHop()
{
	if(meta.my_metadata.fwdHopCount == 0bv8){
		call parse_next_revHop();
	}
}

//Parser State parse_next_revHop
procedure {:inline 1} parse_next_revHop()
{
	if(meta.my_metadata.revHopCount == 0bv8){
		call accept();
	}
}

//Parser State parse_revHop
procedure {:inline 1} parse_revHop()
modifies stack.index, isValid, meta.my_metadata.revHopCount;
{
	call packet_in.extract.headers.axon_revHop.next(hdr.axon_revHop);
	meta.my_metadata.revHopCount := add.bv8(meta.my_metadata.revHopCount, 255bv8);
	call parse_next_revHop();
}

//Parser State start
procedure {:inline 1} start()
modifies meta.my_metadata.fwdHopCount, isValid, meta.my_metadata.revHopCount, meta.my_metadata.headerLen, parser.tmp_0;
{
	havoc parser.tmp_0;
	if(parser.tmp_0[64:0] == 0bv64){
		call parse_head();
	}
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
modifies standard_metadata.egress_spec, drop, hdr.axon_revHop.0.port, forward, isValid, hdr.axon_head.revHopCount, hdr.axon_head.fwdHopCount;
{
	if(hdr.axon_head.axonLength!=meta.my_metadata.headerLen){
		call drop_pkt.apply();
	}
	else {
		call route_pkt.apply();
	}
}

// Action NoAction_0
procedure {:inline 1} NoAction_0()
{
}

// Action NoAction_3
procedure {:inline 1} NoAction_3()
{
}

// Action _drop_0
procedure {:inline 1} _drop_0()
modifies drop;
{
	call mark_to_drop();
}

// Action _drop_2
procedure {:inline 1} _drop_2()
modifies drop;
{
	call mark_to_drop();
}

// Action route_0
procedure {:inline 1} route_0()
modifies standard_metadata.egress_spec, hdr.axon_revHop.0.port, forward, isValid, hdr.axon_head.revHopCount, hdr.axon_head.fwdHopCount;
{
	forward := true;
	standard_metadata.egress_spec := 0bv1++hdr.axon_fwdHop.0.port;
	hdr.axon_head.fwdHopCount := add.bv8(hdr.axon_head.fwdHopCount, 255bv8);
	hdr.axon_head.revHopCount := add.bv8(hdr.axon_head.revHopCount, 1bv8);
	isValid[hdr.axon_revHop[0]] := true;
	hdr.axon_revHop.0.port := standard_metadata.ingress_port[8:0];
}

// Table drop_pkt
procedure {:inline 1} drop_pkt.apply()
modifies drop;
{
	if(drop_pkt.action_run == action._drop_0){
		call _drop_0();
	}
	else {
		call mark_to_drop();
	}
}

// Table route_pkt
procedure {:inline 1} route_pkt.apply()
modifies standard_metadata.egress_spec, drop, hdr.axon_revHop.0.port, forward, isValid, hdr.axon_head.revHopCount, hdr.axon_head.fwdHopCount;
{
	if(route_pkt.action_run == action._drop_2){
		call _drop_2();
	}
	else if(route_pkt.action_run == action.route_0){
		call route_0();
	}
	else {
		call mark_to_drop();
	}
}

// Control DeparserImpl
procedure {:inline 1} DeparserImpl()
modifies emit;
{
	call packet_out.emit.headers.axon_head(hdr.axon_head);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 0);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 1);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 2);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 3);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 4);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 5);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 6);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 7);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 8);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 9);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 10);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 11);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 12);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 13);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 14);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 15);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 16);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 17);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 18);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 19);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 20);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 21);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 22);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 23);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 24);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 25);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 26);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 27);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 28);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 29);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 30);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 31);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 32);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 33);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 34);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 35);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 36);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 37);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 38);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 39);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 40);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 41);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 42);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 43);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 44);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 45);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 46);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 47);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 48);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 49);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 50);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 51);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 52);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 53);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 54);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 55);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 56);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 57);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 58);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 59);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 60);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 61);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 62);
	call packet_out.emit.headers.axon_fwdHop(hdr.axon_fwdHop, 63);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 0);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 1);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 2);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 3);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 4);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 5);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 6);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 7);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 8);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 9);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 10);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 11);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 12);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 13);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 14);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 15);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 16);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 17);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 18);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 19);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 20);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 21);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 22);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 23);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 24);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 25);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 26);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 27);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 28);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 29);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 30);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 31);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 32);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 33);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 34);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 35);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 36);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 37);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 38);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 39);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 40);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 41);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 42);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 43);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 44);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 45);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 46);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 47);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 48);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 49);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 50);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 51);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 52);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 53);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 54);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 55);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 56);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 57);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 58);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 59);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 60);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 61);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 62);
	call packet_out.emit.headers.axon_revHop(hdr.axon_revHop, 63);
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
modifies standard_metadata.egress_spec, drop, meta.my_metadata.fwdHopCount, hdr.axon_revHop.0.port, forward, isValid, meta.my_metadata.revHopCount, meta.my_metadata.headerLen, emit, parser.tmp_0, hdr.axon_head.revHopCount, hdr.axon_head.fwdHopCount;
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

procedure {:inline 1} packet_in.extract.headers.axon_fwdHop.next(stack:HeaderStack)
modifies stack.index, isValid;
{
	isValid[stack[stack.index[stack]]] := true;
	stack.index[stack] := stack.index[stack]+1;
}

procedure {:inline 1} packet_in.extract.headers.axon_revHop.next(stack:HeaderStack)
modifies stack.index, isValid;
{
	isValid[stack[stack.index[stack]]] := true;
	stack.index[stack] := stack.index[stack]+1;
}

procedure packet_out.emit.headers.axon_head(header:Ref);
	ensures isValid[header]!=true || emit[header]==true;
	modifies emit;

procedure {:inline 1} packet_out.emit.headers.axon_fwdHop(stack:HeaderStack, index:int)
modifies emit;
{
	if(isValid[stack[index]]) {
		emit[stack[index]] := true;
	}
}

procedure {:inline 1} packet_out.emit.headers.axon_revHop(stack:HeaderStack, index:int)
modifies emit;
{
	if(isValid[stack[index]]) {
		emit[stack[index]] := true;
	}
}

procedure {:inline 1} mark_to_drop()
modifies drop;
{
	drop := true;
}

procedure clear_drop();
	ensures forward==false;
	modifies drop;
