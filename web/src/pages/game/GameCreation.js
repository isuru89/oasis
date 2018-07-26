import React, { Component } from 'react'
import Form from "antd/lib/form";
import Input from "antd/lib/input";


const formItemLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 14 },
}

export default class GameCreation extends Component {
  render() {
    const { onSubmit } = this.props;

    return (
      <Form onSubmit={onSubmit}>
        <Form.Item {...formItemLayout} label="Name">
          <Input placeholder="name of the game" />
        </Form.Item>
        <Form.Item {...formItemLayout} label="Motto">
          <Input />
        </Form.Item>
      </Form>
    )
  }
}
