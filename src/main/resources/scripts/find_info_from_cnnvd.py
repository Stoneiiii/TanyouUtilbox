# -*- coding: utf-8 -*-

# 导入web端lib库


# 添加正则引用
import re
# 添加BeautifulSoup引用
from bs4 import BeautifulSoup
import xml.etree.ElementTree as ET
import lxml
import sqlite3
import re
import os
import argparse



def load_xml(xml_file_path):
    """
    导入xml的案例，因为没有关闭文件，所以最好不要使用
    :param xml_file_path:
    :return: BeautifulSoup
    """
    xml_content = open(xml_file_path, 'r', encoding="utf-8").read()
    soup = BeautifulSoup(xml_content.strip('\n'), 'lxml')
    a = soup.find_all("entry")
    print(len(a))

    print(a[0].findChild('severity').text)
    print(a[0].findChild('vuln-id').text)

    return soup


def get_cnnvd_severity(soup, vuln_id):
    """
    利用bs4 根据vuln_id获得severity（根据叶子找邻居叶子）
    :param soup:  bs4的soup
    :param vuln_id:
    :return:severity
    """
    try:
        target_node = soup.find(string=vuln_id)  # 找到叶子
        target_parent = target_node.find_parent("entry")  # 找到叶子的爸爸
        target_severity = target_parent.findChild('severity').text  # 找到同爸爸下面的目标节点
    except:
        target_severity = None
    return target_severity



def store_info_to_file(file_path, result_path):
    """
    从CNNVD下载的xml文件中提取信息保存到result_path路径
    :param file_path: CNNVD下载的xml的文件夹目录
    :param result_path: 保存数据路径
    :return:
    """
    try:
        with open(result_path, 'w') as f:
            # 读
            for path in file_path:
                print(path)

                xml_content = open(path, 'r', encoding="utf-8").read()
                soup = BeautifulSoup(xml_content.strip('\n'), 'lxml')
                entrys = soup.find_all("entry")  # 找到所有entry节点
                for entry in entrys:
                    # 写
                    try:
                        vuln_id = entry.findChild('vuln-id').text
                        severity = entry.findChild('severity').text
                        f.write(vuln_id + ':' + severity + '\n')
                    except:
                        f.write('None' + ':' + 'None')
        print("finish!")
        return "success！"
    except:
        return "fail!"
    finally:
        f.close()


def get_xml_path(path):
    """
    获得文件夹下所有文件的详细路径
    :param path:
    :return:
    """
    xml_paths = []
    for dirpath, dirnames, filenames in os.walk(path):
        for filepath in filenames:
            xml_paths.append(os.path.join(dirpath, filepath))
    return xml_paths

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--path',type = str,default = '')
    args = parser.parse_args()
    print(get_xml_path(args.path))