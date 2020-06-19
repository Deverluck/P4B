import os
import time
import commands
from timeit import default_timer as timer

if __name__ == '__main__':
	# dirs = ['axon', 'copy-to-cpu', 'dapper', 'mplb-router',
	# 	'ndp-router', 'p4xos-acceptor', 'resubmit', 'simple-nat',
	# 	'simple-router', 'big-switch']
	dirs = ['axon']
	for dirname in dirs:
		for filename in os.listdir('./'+dirname):
			if filename.find('.bpl') != -1:
				print('*****'+filename+'*****')
				print('	boogie ./'+dirname+'/'+filename+' \/proc mainProcedure')
				starttime = timer()
				(status, output) = commands.getstatusoutput('boogie ./'+dirname+'/'+filename+' \/proc mainProcedure')
				print(output)
				endtime = timer()
				print('	Time: %.1f ms'%((endtime-starttime)*1000))
				f = open('./'+dirname+'/'+filename)
				conts = f.readlines()
				assert_cnt = 0
				for cont in conts:
					if cont.find('assert')!=-1:
						assert_cnt+=1
				print('	assert: '+str(assert_cnt))
				print('	Boogie LOC: '+str(len(conts)))
				f.close()

				p4filename = './'+dirname+'/'+filename[:-4]+'.p4'
				if os.path.exists(p4filename):
					f = open('./'+dirname+'/'+filename[:-4]+'.p4')
					conts = f.readlines()
					print('	P4 LOC: '+str(len(conts)))
					table_cnt = 0
					action_cnt = 0
					for cont in conts:
						if cont.find('table ') != -1:
							table_cnt += 1
						if cont.find('action ')!=-1 and cont.find('default_action ')==-1:
							action_cnt += 1
					print('	table: '+str(table_cnt))
					print('	action: '+str(action_cnt))
					f.close()
				print('*****'+filename+' ends*****')
				print(' ')