import sys
sys.setrecursionlimit(2**31-1)

import copy

def read_input(filename):
    G = []
    T = []
    file = open(filename)
    n = int(file.readline())
    for s in range(n):
        T.append(int(file.readline()))
        G.append([])
        for action in file.readline().split(','):
            action = action.split()
            probs  = [float(p) for p in action[0::2]]
            states = [int(ss) for ss in action[1::2]]
            """
            if sum(probs) != 1:
                sys.exit('action error at state ' + str(s))
            if [ss for ss in states if ss < 0 or ss >= n] != []:
                sys.exit('action error at state ' + str(s))
            """
            G[-1].append(dict(zip(states,probs)))
    return G,T

def dfs1(MM, s, stack, visit1):
    visit1[s] = 1
    if len(MM[s]) == 0:
        stack.append(s)
        return
    for ss in MM[s]:
        if not visit1[ss]:
            dfs1(MM, ss, stack, visit1)
    stack.append(s)

def dfs2(M, s, aset, visit2):
    visit2[s] = 1
    aset.add(s)
    if len(M[s]) == 0: return
    for ss in M[s]:
        if not visit2[ss]:
            dfs2(M, ss, aset, visit2)

def scc(M):
    MM = {}
    visit1 = {}
    visit2 = {}
    for s in M:
        if s not in MM:
            MM[s] = set()
        visit1[s] = 0
        visit2[s] = 0
        for ss in M[s]:
            if ss not in MM:
                MM[ss] = set()
            MM[ss].add(s)
    stack = []
    for s in M:
        if not visit1[s]:
            dfs1(MM, s, stack, visit1)

    sol = []
    for s in stack[::-1]:
        if not visit2[s]:
            aset = set()
            dfs2(M, s, aset, visit2)
            sol.append(aset)

    return sol

def main():
    G,T = read_input(sys.argv[1].strip())

    GG = copy.deepcopy(G)

    # find all MEC
    stack = [set(range(len(G)))]
    mec = []
    while len(stack) != 0:
        states = stack.pop()
        M = {}
        for s in states:
            M[s] = set()
            no_out = []
            for a in range(len(GG[s])):
                for ss in GG[s][a]:
                    if ss not in states:
                        break
                else:
                    no_out.append(GG[s][a])
                    M[s].update(GG[s][a])
            GG[s] = no_out
        # find SCC
        components = scc(M)
        if len(components) == 1:
            mec.append(components[0])
        else:
            stack += components

    cnt = 0
    for m in mec:
        if len(m) == 1:
            s = min(m)
            if {s: 1.0} not in G[s]: continue
        cnt += 1
    print(cnt)

if __name__ == '__main__': main()
