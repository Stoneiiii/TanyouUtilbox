# 添加BeautifulSoup引用
from bs4 import BeautifulSoup
# 添加BeautifulSoup引用中使用了lxml解析器
import lxml
import re
import os
import time
import argparse


def store_info_to_file(file_path, result_path):
    """
    从CNNVD下载的xml文件中提取信息保存到result_path路径
    :param file_path: CNNVD下载的xml的文件夹目录
    :param result_path: 保存数据路径
    :return: none
    """
    n = 0
    try:
        with open(result_path, 'w') as f:
            # 读
            for path in file_path:
                # print(path)

                xml_content = open(path, 'r', encoding="utf-8").read()
                cleaned_xml_content = data_cleaning(xml_content)
                soup = BeautifulSoup(cleaned_xml_content.strip('\n'), 'lxml')
                entrys = soup.find_all("entry")  # 找到所有entry节点
                for entry in entrys:
                    # 写
                    try:
                        name = entry.findChild('name').text
                        vuln_id = entry.findChild('vuln-id').text
                        published_time = entry.findChild('published').text
                        modified_time = entry.findChild('modified').text
                        severity = entry.findChild('severity').text
                        vuln_type = entry.findChild('vuln-type').text
                        # # 用正则获取 vuln_descript和 cve_id
                        # 已经清洗数据了，不再使用
                        # vuln_descript = "NONE"
                        # pattern = '(?<=<vuln-descript>).*(?=</vuln-descript>)'
                        # res = re.search(pattern, str(entry))
                        # if res != None:
                        #     vuln_descript = res.group()
                        # cve_id = "NONE"
                        # pattern = '(?<=<cve-id>).*(?=</cve-id>)'
                        # res = re.search(pattern, str(entry))
                        # if res != None:
                        #     cve_id = res.group()
                        vuln_descript = entry.findChild('vuln-descript').text
                        cve_id = entry.findChild('cve-id').text

                        # 保留此数据，后期数据库对齐遇到的时候，交给评审
                        # if name == '编号重复':
                        #     print(vuln_descript)
                        #     continue

                        f.write('@h&lname@h&l:' + name + "@l&hname@l&h" +
                                '@h&lvuln_id@h&l:' + vuln_id + '@l&hvuln_id@l&h' +
                                '@h&lpublished_time@h&l:' + published_time + '@l&hpublished_time@l&h' +
                                '@h&lmodified_time@h&l:' + modified_time + '@l&hmodified_time@l&h' +
                                '@h&lseverity@h&l:' + severity + '@l&hseverity@l&h' +
                                '@h&lvuln_type@h&l:' + vuln_type + '@l&hvuln_type@l&h' +
                                '@h&lvuln_descript@h&l:' + vuln_descript + '@l&hvuln_descript@l&h' +
                                '@h&lcve_id@h&l:' + cve_id + '@l&hcve_id@l&h' +
                                '\n')
                    except:
                        f.write('@h&lname@h&l:' + 'NONE' + "@l&hname@l&h" +
                                '@h&lvuln_id@h&l:' + 'NONE' + '@l&hvuln_id@l&h' +
                                '@h&lpublished_time@h&l:' + 'NONE' + '@l&hpublished_time@l&h' +
                                '@h&lmodified_time@h&l:' + 'NONE' + '@l&hmodified_time@l&h' +
                                '@h&lseverity@h&l:' + 'NONE' + '@l&hseverity@l&h' +
                                '@h&lvuln_type@h&l:' + 'NONE' + '@l&hvuln_type@l&h' +
                                '@h&lvuln_descript@h&l:' + 'NONE' + '@l&hvuln_descript@l&h' +
                                '@h&lcve_id@h&l:' + 'NONE' + '@l&hcve_id@l&h' +
                                '\n')
                n = n + 1
        print("finish!")
        print("文件数：" + str(n))
    finally:
        f.close()


def data_cleaning(content: str):
    """
    XML数据清洗：清楚cnnvd标签里text部分未转义的字符：< > & ' "
    :param content: 需要被清洗的文本字符串
    :return: 清洗后的字符串
    """
    # 排除的xml标签
    elements = ['<?xml version="1.0" encoding="UTF-8"?>',
                '<cnnvd cnnvd_xml_version="*.*" pub_date="****-**-**" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">',
                '<entry>', '</entry>',
                '<name>', '</name>',
                '<vuln-id>', '</vuln-id>',
                '<published>', '</published>',
                '<modified>', '</modified>',
                '<source>', '</source>',
                '<severity>', '</severity>',
                '<vuln-type>', '</vuln-type>',
                '<vuln-descript>', '</vuln-descript>',
                '<other-id>', '</other-id>',
                '<cve-id>', '</cve-id>',
                '<bugtraq-id>', '</bugtraq-id>',
                '<vuln-solution>', '</vuln-solution>',
                '</cnnvd>']
    # 排除的XML标签加密
    encrypt_escape_char_dic = {'<': '%avlyunlt;%avlyun',
                               '>': '%avlyungt;%avlyun',
                               '&': '%avlyunamp;%avlyun',
                               '\'': '%avlyunapos;%avlyun',
                               '"': '%avlyunquot;%avlyun'
                               }
    # 给需要排除的XML标签加密 形成字典
    encrypt_elements_dic = {}
    decrypt_elements_dic = {}
    for element in elements:
        pattern = re.compile("|".join(map(re.escape, encrypt_escape_char_dic.keys())))
        encrypt_element = pattern.sub(lambda m: encrypt_escape_char_dic[m.group()], element)
        encrypt_elements_dic[element] = encrypt_element
        decrypt_elements_dic[encrypt_element] = element
    # 先替换排除xml的标签
    pattern1 = re.compile("|".join(map(re.escape, encrypt_elements_dic.keys())))
    encrypt_content = pattern1.sub(lambda m: encrypt_elements_dic[m.group()], content)
    # 清洗未转义的xml字符
    escape_char_dic = {'<': '&lt;',
                       '>': '&gt;',
                       '&': '&amp;',
                       '\'': '&apos;',
                       '"': '&quot;'
                       }
    pattern2 = re.compile("|".join(map(re.escape, escape_char_dic.keys())))
    cleaned_data = pattern2.sub(lambda m: escape_char_dic[m.group()], encrypt_content)
    # 还原加密的数据
    pattern3 = re.compile("|".join(map(re.escape, decrypt_elements_dic.keys())))
    data = pattern3.sub(lambda m: decrypt_elements_dic[m.group()], cleaned_data)

    return data


def get_xml_path(path):
    """
    获得文件夹下所有文件的详细路径
    :param path: XML的存放文件夹路径
    :return:所有xml的绝对路径
    """
    xml_paths = []
    for dirpath, dirnames, filenames in os.walk(path):
        for filepath in filenames:
            xml_paths.append(os.path.join(dirpath, filepath))
    return xml_paths


def data_wash(path):
    """
    字典文件CVE相同去重，保留时间最新的信息
    1.认为时间不同的数据也要保留，数据一条CVE对应多条CNNVD，如果后期对齐探优数据库中遇到，则单独输出整行update的SQL，交付评审。
    2.发现时间最新的可能为重复数据（name字段显示为编号重复），需要排除
    :param path: 字典文件路径
    :return:null
    """
    n = 0  # 计算器: 重复条数
    dic = {}
    f = open(path, 'r+')
    ft = open('tmp', 'w+')
    lines = f.readlines()
    for line in lines:
        # 提取出更新时间
        pattern = '(?<=@h&lmodified_time@h&l:).*(?=@l&hmodified_time@l&h)'
        res = re.search(pattern, line)
        modify_time = res.group()
        # 提取出CVE编号
        pattern = '(?<=@h&lcve_id@h&l:).*(?=@l&hcve_id@l&h)'
        res = re.search(pattern, line)
        cve_id = res.group()
        # cve编号是否合法，不合法的直接写入缓存，保留数据
        pattern = r'^CVE-\d{4}-\d{1,}$'
        res = re.search(pattern, cve_id)
        if res is None:
            ft.write(line)
            continue
        # 去重
        if dic.get(cve_id) is None:
            dic[cve_id] = [line, modify_time]
        else:
            # 排除name值是编号重复的情况
            pattern = '(?<=@h&lname@h&l:).*(?=@l&hname@l&h)'
            res = re.search(pattern, line)
            name = res.group()
            if name == '编号重复':
                continue
            # 对比时间
            timestamp = time.mktime(time.strptime(dic.get(cve_id)[1], "%Y-%m-%d"))
            cur_timestamp = time.mktime(time.strptime(modify_time, "%Y-%m-%d"))
            if cur_timestamp > timestamp:
                # 更新为最新时间和信息
                dic[cve_id] = [line, modify_time]
                n = n + 1  # 计算器+1

    f.close()
    # 字典数据写入缓存
    for val in dic.values():
        ft.write(val[0])
    ft.close()
    # 缓存数据写入原始数据
    f = open(path, 'w+')
    ft = open('tmp', 'r')
    f.write(ft.read())
    f.close()
    ft.close()
    # 删除缓存文件
    os.remove('tmp')

    print("重复数量：" + str(n))


def data_deduplicate(path):
    """
    字典文件去重，完全重复的行，删除重复行
    :param path:
    :return:
    """
    data = set()
    f = open(path, 'r+')
    ft = open('tmp', 'w+')
    n = 0  # 计数器：完全重复行的数量
    lines = f.readlines()
    for line in lines:
        cur_len = len(data)
        data.add(line)
        if cur_len == len(data):
            n = n + 1
            # print(line)
    f.close()

    # 写入缓存
    for val in data:
        ft.write(val)
    ft.close()
    # 缓存写入文件
    f = open(path, 'w+')
    ft = open('tmp', 'r')
    f.write(ft.read())
    f.close()
    ft.close()
    # 删除缓存文件
    os.remove('tmp')

    print('重复数量：' + str(n))


if __name__ == "__main__":

    # 字典cve编号去重
    # 目前认为CVE重复的数据需要保留，如果后期对齐探优数据库中遇到，则单独输出整行update的SQL，交付评审。所以此方法废弃
    # ！！禁止使用！！
    # dic_path = r'/media/stone/data/myproject/python/modify_cve_cnvd/cnnvd_all_info000.txt'
    # data_wash(dic_path)

    # 字典删除完全重复的行
    # dic_path = r'/media/stone/data/myproject/python/modify_cve_cnvd/cnnvd_all_info_test.txt'
    # data_deduplicate(dic_path)

    parser = argparse.ArgumentParser()
    parser.add_argument('--file_path', type=str, default='')
    parser.add_argument('--dic_path', type=str, default='')
    args = parser.parse_args()

    # 转存信息到文档
    xml_paths = get_xml_path(args.file_path)
    result_path = r'/media/stone/data/myproject/python/modify_cve_cnvd/cnnvd_all_info.txt'
    store_info_to_file(xml_paths, args.dic_path)
    # 字典删除完全重复的行
    data_deduplicate(args.dic_path)

    pass
