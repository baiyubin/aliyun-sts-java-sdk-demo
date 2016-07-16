package com.aliyun.oss.demo;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;

/**
 * StsServiceSample
 */
public class StsServiceSample {

    // 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    // 当前 STS API 版本
    public static final String STS_API_VERSION = "2015-04-01";
    // STS服务必须为 HTTPS
    public static final ProtocolType STS_PROTOCOL_TYPE = ProtocolType.HTTPS;
    
    static AssumeRoleResponse assumeRole(String accessKeyId,String accessKeySecret, String roleArn, 
            String roleSessionName) throws ClientException {
        return assumeRole(accessKeyId, accessKeySecret, roleArn, roleSessionName, null, 3600, 
                STS_PROTOCOL_TYPE);
    }
    
    static AssumeRoleResponse assumeRole(String accessKeyId,String accessKeySecret, String roleArn, 
            String roleSessionName, String policy) throws ClientException {
        return assumeRole(accessKeyId, accessKeySecret, roleArn, roleSessionName, policy, 3600, 
                STS_PROTOCOL_TYPE);
    }

    static AssumeRoleResponse assumeRole(String accessKeyId,String accessKeySecret, String roleArn, 
            String roleSessionName, String policy, long expired, ProtocolType protocolType)
                    throws ClientException {
        // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
        IClientProfile profile = DefaultProfile.getProfile(REGION_CN_HANGZHOU, 
                accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        // 创建一个 AssumeRoleRequest 并设置请求参数
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setVersion(STS_API_VERSION);
        request.setMethod(MethodType.POST);
        request.setProtocol(protocolType);
        request.setRoleArn(roleArn);
        request.setRoleSessionName(roleSessionName);
        request.setPolicy(policy);
        request.setDurationSeconds(expired);

        // 发起请求，并得到response
        return client.getAcsResponse(request);
    }

    public static void main(String[] args) {

        // 只有 子账号才能调用 AssumeRole接口
        // 阿里云主账号的AccessKeys不能用于发起AssumeRole请求
        // 请首先在RAM控制台创建子用户，并为这个用户创建AccessKeys
        //String accessKeyId = "b6R5PxQSYFGhSQmR";
        //String accessKeySecret = "QzubQVKg8Dzzc6f6LeCdDQTbDLPjF4";
        String accessKeyId = "Qllj3v3UZ00jzNr5";
        String accessKeySecret = "KSANDU3oU95oAL8K8lY3kjiOy1IM6C";
        
        // AssumeRole API 请求参数: RoleArn, RoleSessionName, Policy, and
        // DurationSeconds
        // RoleArn可以到控制台上获取，路径是 访问控制 > 角色管理 > 角色名称 > 基本信息 > Arn
        String roleArn = "acs:ram::1085846427845250:role/accessossforinnerrole";
        
        // RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
        // 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '_' 字母和数字等字符
        // 具体规则请参考API文档中的格式要求
        String roleSessionName = "alice-001";

        // 如何定制你的policy，如果policy为null，则STS的权限与roleArn的policy的定义的权限
        String policy = "{\n" +
                "    \"Version\": \"1\", \n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"oss:GetBucket\", \n" +
                "                \"oss:PutObject\", \n" +
                "                \"oss:GetObject\", \n" +
                "                \"oss:ListParts\" \n" +
                "            ], \n" +
                "            \"Resource\": [\n" +
                "                \"acs:oss:*:*:*\"\n" +
                "            ], \n" +
                "            \"Effect\": \"Allow\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // 过期时间设置默认是一小时，单位秒有效值是[900, 3600]，即15分钟到60分钟。
        long expired = 3600;

        try {
            AssumeRoleResponse response = assumeRole(accessKeyId,accessKeySecret, roleArn, 
                    roleSessionName, policy, expired, STS_PROTOCOL_TYPE);

            System.out.println("Expiration: " + response.getCredentials().getExpiration());
            System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
            System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
            System.out.println("Security Token: " + response.getCredentials().getSecurityToken());
        } catch (ClientException e) {
            System.out.println("Error code: " + e.getErrCode());
            System.out.println("Error message: " + e.getErrMsg());
        }
        
    }

}
