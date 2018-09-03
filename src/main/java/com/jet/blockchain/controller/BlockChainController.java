package com.jet.blockchain.controller;

import com.jet.blockchain.model.BlockChain;
import com.jet.blockchain.model.Transaction;
import com.jet.blockchain.vo.RegisterRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
* @Author: Jet.Chen
* @Date: 2018/9/3
*/
@Scope("prototype")
@Controller
@RequestMapping(value = "/")
@Api(consumes = "application/json",
        produces = "application/json",
        protocols = "http",
        basePath = "/", value = "区块链示例")
public class BlockChainController {

    @Autowired
    @Qualifier("blockChain")
    private BlockChain blockChain;

    @Autowired
    @Qualifier("nodeId")
    private String nodeId;

    @GetMapping(value = "/chain")
    @ResponseBody
    @ApiOperation(value = "查看完整的区块链")
    public Map<String, Object> fullChain() {
        Map<String, Object> map = new HashMap<>();
        map.put("chain", blockChain.getChain());
        map.put("length", blockChain.getChain().size());
        return map;
    }


    @PostMapping(value = "/transactions/new")
    @ResponseBody
    @ApiOperation(value = "创建新交易")
    public Map<String, Object> newTransaction(@RequestBody Transaction transaction) {
        long index = blockChain.newTransaction(transaction.getSender(),
                transaction.getRecepient(),
                transaction.getAmount());
        Map<String, Object> map = new HashMap<>();
        map.put("message", String.format("Transaction will be added to Block %d", index));
        return map;
    }

    @GetMapping(value = "/mine")
    @ResponseBody
    @ApiOperation(value = "挖矿")
    public Map<String, Object> mine() {
        Map<String, Object> map = new HashMap<>();
        BlockChain lastBlock = blockChain.getLastBlock();
        Integer lastProof = lastBlock.getProof();
        Integer proof = blockChain.proofOfWork(lastProof);
        blockChain.newTransaction("0", nodeId, BigDecimal.ONE);
        BlockChain block = blockChain.newBlock(proof, lastBlock.getHash());
        map.put("message", "New Block Forged");
        map.put("index", block.getIndex());
        map.put("transactions", block.getTransactions());
        map.put("proof", block.getProof());
        map.put("previousHash", block.getPreviousHash());
        return map;
    }


    @PostMapping(value = "/register")
    @ResponseBody
    @ApiOperation(value = "注册集群节点")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        if (CollectionUtils.isNotEmpty(request.getNodes())) {
            for (String n : request.getNodes()) {
                blockChain.registerNode(n);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("message", "New nodes have been added");
        map.put("totalNodes", blockChain.getNodes());
        return map;
    }


    @GetMapping(value = "/resolve")
    @ResponseBody
    @ApiOperation(value = "解决不同节点间的数据冲突")
    public Map<String, Object> resolve() {
        boolean replaced = blockChain.resolveConflicts();
        Map<String, Object> map = new HashMap<>();
        if (replaced) {
            map.put("message", "Our chain was replaced");
            map.put("newChain", blockChain.getChain());
        } else {
            map.put("message", "Our chain is authoritative");
            map.put("chain", blockChain.getChain());
        }
        return map;
    }


    @GetMapping(value = "/validate")
    @ResponseBody
    @ApiOperation(value = "验证区块链自身是否合法")
    public Map<String, Object> validate() {
        boolean result = blockChain.validChain(blockChain.getChain());
        Map<String, Object> map = new HashMap<>();
        if (result) {
            map.put("message", "this chain is valid");
        } else {
            map.put("message", "this chain is illegal");
        }
        return map;
    }
}