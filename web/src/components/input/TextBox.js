import React, { Component } from 'react'
import styled from 'styled-components';

const InputText = styled.input`
  border: 2px solid #20465EAA;
  border-radius: 3px;

  &:focus {
    border: 2px solid #20465E;
  }
`

export default class TextBox extends Component {
  render() {

    return (
        <InputText type="text" {...this.props} /> 
    )
  }
}
