import React, { Component } from 'react'
import Form from "antd/lib/form";
import Input from "antd/lib/input";

const TextArea = Input.TextArea;

const formItemLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 14 },
}

export default class CreateKpiForm extends Component {
  render() {
    const { onSubmit } = this.props;

    return (
      <Form onSubmit={onSubmit}>
        <Form.Item {...formItemLayout} label="Name">
          <Input />
        </Form.Item>
        <Form.Item {...formItemLayout} label="For Event">
          <Input />
        </Form.Item>
        <Form.Item {...formItemLayout} label="Field Name">
          <Input />
        </Form.Item>
        <Form.Item {...formItemLayout} label="Expression">
          <TextArea autosize={{ minRows: 4 }} />
        </Form.Item>
      </Form>
    )
  }
}
